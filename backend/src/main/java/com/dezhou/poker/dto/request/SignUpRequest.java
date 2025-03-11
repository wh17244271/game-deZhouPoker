package com.dezhou.poker.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 注册请求DTO
 */
@Data
public class SignUpRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    private String password;

    private BigDecimal initialChips = BigDecimal.valueOf(1000);
}
