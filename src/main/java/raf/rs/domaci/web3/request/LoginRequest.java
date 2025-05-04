package raf.rs.domaci.web3.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;
}
