package com.innowise.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto {
    @NotBlank(message = "Login is required")
    @Length(min = 3, max = 20, message = "Login must have length between 3 and 20")
    private String login;

    @NotBlank(message = "Password is required")
    @Length(min = 8, max = 20, message = "Password must have length between 8 and 20")
    private String password;

    @NotNull(message = "User id is required")
    private Long userId;
}
