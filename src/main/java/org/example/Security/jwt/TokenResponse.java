package org.example.Security.jwt;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenResponse {
    private String jwtToken;

    public TokenResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
