package raf.rs.domaci.web3.response;

import lombok.Getter;
import lombok.Setter;
import raf.rs.domaci.web3.model.ErrorMessage;

import java.time.LocalDateTime;
@Getter
@Setter
public class ErrorMessageResponse {
    private Long id;
    private String message;
    private LocalDateTime timestamp;

    public ErrorMessageResponse(Long id, String message, LocalDateTime timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }


    // Factory metod
    public static ErrorMessageResponse fromEntity(ErrorMessage entity) {
        return new ErrorMessageResponse(entity.getId(), entity.getMessage(), entity.getTimestamp());
    }
}