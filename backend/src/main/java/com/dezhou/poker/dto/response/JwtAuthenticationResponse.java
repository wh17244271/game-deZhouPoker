package com.dezhou.poker.dto.response;

import lombok.Data;

/**
 * JWT认证响应DTO
 */
@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;

    public JwtAuthenticationResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
    }
}
