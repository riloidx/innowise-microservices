package com.innowise.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginDto {
    @NotBlank(message = "Login is required")
    @Length(min = 3, max = 20, message = "Login must have length between 3 and 20")
    private String login;

    @NotBlank(message = "Password is required")
    @Length(min = 8, max = 20, message = "Password must have length between 8 and 20")
    private String password;
}
