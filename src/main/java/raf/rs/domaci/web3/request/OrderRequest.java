package raf.rs.domaci.web3.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import raf.rs.domaci.web3.model.Dish;

import java.util.List;

@Data
@Getter
@Setter
public class OrderRequest {

    private Long userId;
    private List<Long> dishIDs;
    private String scheduledTime;

}
