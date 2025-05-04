package raf.rs.domaci.web3.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raf.rs.domaci.web3.model.Dish;
import raf.rs.domaci.web3.model.Order;
import raf.rs.domaci.web3.model.Status;
import raf.rs.domaci.web3.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Status status;
    private boolean active;
    private User createdBy;
    private List<DishDTO> items;

    public OrderResponse(Long id, Status status, boolean active, User createdByUsername) {
        this.id = id;
        this.status = status;
        this.active = active;
        this.createdBy = createdByUsername;
    }


    public static OrderResponse fromEntity(Order order) {


        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.isActive(),
                order.getCreatedBy()
        );
    }
    public static OrderResponse fromEntity(Order order, List<Dish> dishes) {
        List<DishDTO> dishDTOs = dishes.stream()
                .map(d -> new DishDTO(d.getId(), d.getName()))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.isActive(),
                order.getCreatedBy(),
                dishDTOs
        );
    }
}