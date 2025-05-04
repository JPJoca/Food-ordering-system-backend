package raf.rs.domaci.web3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import raf.rs.domaci.web3.model.*;
import raf.rs.domaci.web3.repositories.ErrorMessageRepository;
import raf.rs.domaci.web3.repositories.OrderRepository;
import raf.rs.domaci.web3.request.OrderRequest;
import raf.rs.domaci.web3.response.OrderResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OrderService implements  IService<Order,Long> {

    @Autowired
    private ErrorMessageRepository errorMessageRepository;
    private final OrderRepository orderRepository;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>(); // orderId -> task
    private final Semaphore activeOrdersSemaphore = new Semaphore(3);

    @Autowired
    public OrderService(OrderRepository orderRepository, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Order placeOrder(Order order) {
        try {
            if (activeOrdersSemaphore.availablePermits() == 0) {
                logError("Cannot place new order: maximum active order limit reached.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot place new order: maximum active order limit reached.");
            }

            return orderRepository.save(order);

        } catch (ResponseStatusException e) {
            throw e; // prosledi dalje
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to place order.");
        }
    }

   public List<Order> findByUserId(Long userId) {
        return this.orderRepository.findByUserIdOrAll(userId);
    }

    public List<Order> findByStatuses(List<Status> statuses, Long userId) {
        return this.orderRepository.findByStatuses(userId,statuses );
    }

    public Order cancelOrder(Order order) {

        if (!order.getStatus().equals(Status.ORDERED)) {
            throw new IllegalStateException("Cannot cancel an order that is not in ORDERED status.");
        }

        // CANCEL SCHEDULED TASK
        ScheduledFuture<?> future = scheduledTasks.get(order.getId());
        if (future != null) {
            future.cancel(true);
            scheduledTasks.remove(order.getId());
            activeOrdersSemaphore.release();
            System.out.println("Canceled scheduled status update for order " + order.getId());
        }


        order.setStatus(Status.CANCELED);
        order.setActive(false);
        // šaljemo update preko websocket-a
        messagingTemplate.convertAndSend("/topic/orders", order);
        return orderRepository.save(order);


    }


    public void scheduleOrder(OrderRequest orderRequest, List<Dish> dishes, Optional<User> userOpt) {

        LocalDateTime scheduledDateTime;
        try {
            System.out.println(orderRequest.getScheduledTime());
            scheduledDateTime = LocalDateTime.parse(orderRequest.getScheduledTime());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid datetime format");
        }

        LocalDateTime now = LocalDateTime.now();
        long delaySeconds = Duration.between(now, scheduledDateTime).getSeconds();

        if (delaySeconds <= 0) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        if (activeOrdersSemaphore.availablePermits() == 0) {
            logError("Cannot schedule order: maximum number of active orders reached.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot schedule order: maximum number of active orders reached.");
        }
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        System.out.println("Order placed at scheduled time: " + scheduledDateTime);
        scheduler.schedule(() -> {
            Order order = new Order();
            order.setActive(true);
            order.setStatus(Status.ORDERED);
            order.setItems(dishes);
            order.setCreatedBy(userOpt.get());

            Order savedOrder = placeOrder(order);
            scheduleStatusUpdates(savedOrder);

            OrderResponse dto = OrderResponse.fromEntity(savedOrder,dishes);
            messagingTemplate.convertAndSend("/topic/orders", dto);

        }, delaySeconds, TimeUnit.SECONDS);
    }

    @Override
    public <S extends Order> S save(S var1) {
        return this.orderRepository.save(var1);
    }

    @Override
    public Optional<Order> findById(Long var1) {
        return this.orderRepository.findById(var1);
    }

    @Override
    public List<Order> findAll() {
        return this.orderRepository.findAll();
    }

    @Override
    public void deleteById(Long var1) {
        this.orderRepository.deleteById(var1);
    }

    public void scheduleStatusUpdates(Order order) {
        if (!order.getStatus().equals(Status.ORDERED)) {
            return;
        }

        Random random = new Random();
        int preparingDelay = 10 + random.nextInt(6);
        ScheduledFuture<?> preparingFuture = scheduler.schedule(() -> {

            try {
                if (!activeOrdersSemaphore.tryAcquire(0, TimeUnit.SECONDS)) {
                    logError("Order " + order.getId() + " is waiting to enter PREPARING. All slots are currently full.");
                    // sad čekamo normalno dok slot ne bude slobodan
                    activeOrdersSemaphore.acquire();
                }

                Order currentOrder = orderRepository.findById(order.getId()).orElse(null);
                if (currentOrder == null || !currentOrder.getStatus().equals(Status.ORDERED)) {
                    System.out.println("Skipping preparing transition for order " + order.getId());
                    activeOrdersSemaphore.release(); // vrati slot jer nije ispravno stanje
                    return;
                }

                currentOrder.setStatus(Status.PREPARING);
                orderRepository.save(currentOrder);

                OrderResponse dto = OrderResponse.fromEntity(currentOrder);
                messagingTemplate.convertAndSend("/topic/orders", dto);

                System.out.println("Order " + order.getId() + " -> PREPARING");

                // sledeći korak
                schedulePreparingToDelivery(currentOrder);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for semaphore: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while sending order update: " + e.getMessage());
            }

        }, preparingDelay, TimeUnit.SECONDS);

        scheduledTasks.put(order.getId(), preparingFuture);
    }

    private void schedulePreparingToDelivery(Order order) {
        Random random = new Random();
        int deliveryDelay = 15 + random.nextInt(6); // 15 + [0-5]

        ScheduledFuture<?> deliveryFuture = scheduler.schedule(() -> {
            Order currentOrder = orderRepository.findById(order.getId()).orElse(null);
            if (currentOrder == null || !currentOrder.getStatus().equals(Status.PREPARING)) {
                System.out.println("Skipping in delivery transition for order " + order.getId());
                return;
            }

            currentOrder.setStatus(Status.IN_DELIVERY);
            orderRepository.save(currentOrder);
            System.out.println("Order " + order.getId() + " -> IN_DELIVERY");

            try {
                OrderResponse dto = OrderResponse.fromEntity(currentOrder);
                messagingTemplate.convertAndSend("/topic/orders", dto);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while sending order update: " + e.getMessage());
            }

            // Sledeći transition ka DELIVERED
            scheduleDeliveryToDelivered(currentOrder);

        }, deliveryDelay, TimeUnit.SECONDS);

        scheduledTasks.put(order.getId(), deliveryFuture);
    }

    private void scheduleDeliveryToDelivered(Order order) {
        Random random = new Random();
        int deliveredDelay = 20 + random.nextInt(6); // 20 + [0-5]
        System.out.println("radimo delivery to delivered od " + deliveredDelay + " sekundi");
        ScheduledFuture<?> deliveredFuture = scheduler.schedule(() -> {
            Order currentOrder = orderRepository.findById(order.getId()).orElse(null);
            if (currentOrder == null || !currentOrder.getStatus().equals(Status.IN_DELIVERY)) {
                System.out.println("Skipping delivered transition for order " + order.getId());
                return;
            }

            currentOrder.setStatus(Status.DELIVERED);
            orderRepository.save(currentOrder);
            System.out.println("Order " + order.getId() + " -> DELIVERED");

            try {
                OrderResponse dto = OrderResponse.fromEntity(currentOrder);
                messagingTemplate.convertAndSend("/topic/orders", dto);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while sending order update: " + e.getMessage());
            }

            // Kada završi, brišemo iz scheduledTasks
            scheduledTasks.remove(order.getId());
            activeOrdersSemaphore.release();;

        }, deliveredDelay, TimeUnit.SECONDS);

        scheduledTasks.put(order.getId(), deliveredFuture);
    }

    private void logError(String msg) {
        ErrorMessage error = new ErrorMessage();
        error.setMessage(msg);
        error.setTimestamp(LocalDateTime.now());
        errorMessageRepository.save(error);
    }

}


