package raf.rs.domaci.web3.response;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}
