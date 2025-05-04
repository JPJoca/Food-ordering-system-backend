package raf.rs.domaci.web3.request;

import lombok.Getter;
import lombok.Setter;
import raf.rs.domaci.web3.model.Status;

import java.util.List;
@Getter
@Setter
public class StatusRequest {
    private Long userID;
    private List<Status> statuses;

}
