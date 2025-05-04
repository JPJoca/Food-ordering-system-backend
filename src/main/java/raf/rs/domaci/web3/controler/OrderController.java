package raf.rs.domaci.web3.controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import raf.rs.domaci.web3.model.Dish;
import raf.rs.domaci.web3.model.Order;
import raf.rs.domaci.web3.model.Status;
import raf.rs.domaci.web3.model.User;
import raf.rs.domaci.web3.request.CancelRequest;
import raf.rs.domaci.web3.request.OrderRequest;
import raf.rs.domaci.web3.request.StatusRequest;
import raf.rs.domaci.web3.service.DishService;
import raf.rs.domaci.web3.service.OrderService;
import raf.rs.domaci.web3.service.UsersService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("api/order")
public class OrderController {

    private final OrderService orderService;
    private final UsersService usersService;
    private final DishService dishService;

    @Autowired
    public OrderController(OrderService orderService, UsersService usersService, DishService dishService) {
        this.orderService = orderService;
        this.usersService = usersService;
        this.dishService = dishService;
    }

    @MessageMapping("/sendMessage")  // Kada front šalje na /app/sendMessage
    @SendTo("/topic/messages")       // Poruka se prosleđuje na /topic/messages
    public String handleMessage(String message) {
        return message;
    }

    @PreAuthorize("hasAuthority('can_search_order_all') or hasAuthority('can_search_order') && #userId != null")
    @GetMapping(value = "/user-search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Order>> getOrders(@RequestParam(required = false) Long userId) {
        System.out.println(userId);
        System.out.println("ODRADJENO get  Orders");
        List<Order> orders = orderService.findByUserId(userId);
        System.out.println(orders.size());
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAuthority('can_search_order')")
    @PostMapping(value = "/status-search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Order>> getOrders(@RequestBody StatusRequest request) {
        System.out.println(request.getUserID());
        List<Order> orders = orderService.findByStatuses(request.getStatuses(), request.getUserID());
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasAuthority('can_place_order')")
    @PostMapping(value = "/place-order",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest orderRequest) {

        List<Dish> dishes = dishService.findAllById(orderRequest.getDishIDs()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());;

        if (dishes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Order order = new Order();
        order.setActive(true);
        order.setStatus(Status.ORDERED);
        order.setItems(dishes);
        usersService.findById(orderRequest.getUserId()).ifPresent(order::setCreatedBy);
        Order savedOrder = orderService.placeOrder(order);
        orderService.scheduleStatusUpdates(savedOrder);
        return ResponseEntity.ok(savedOrder);
    }

    @PreAuthorize("hasAuthority('can_place_order')")
    @PostMapping(value = "/schedule-order", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> scheduleOrder(@RequestBody OrderRequest orderRequest)
    {
        List<Dish> dishes = dishService.findAllById(orderRequest.getDishIDs()).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ;

        if (dishes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No dishes found"));
        }

        Optional<User> userOpt = usersService.findById(orderRequest.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No user found"));
        }

        try {
            orderService.scheduleOrder(orderRequest, dishes, userOpt);
            return ResponseEntity.ok(Map.of("message", "Order scheduled successfully for " + orderRequest.getScheduledTime()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to schedule order"));
        }
    }

    @PreAuthorize("hasAuthority('can_cancel_order')")
    @PutMapping(value = "/cancel-order",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> cancelOrder(@RequestBody CancelRequest request) {
        Order order = orderService.findById(request.getOrderID()).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        Order canceledOrder = orderService.cancelOrder(order);
        return ResponseEntity.ok(canceledOrder);
    }



}
