package com.dezhou.poker.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
