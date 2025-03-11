package com.dezhou.poker.dto.response;

import lombok.Data;

/**
 * API响应DTO
 */
@Data
public class ApiResponse {
    private Boolean success;
    private String message;
    private Object data;

    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(Boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
