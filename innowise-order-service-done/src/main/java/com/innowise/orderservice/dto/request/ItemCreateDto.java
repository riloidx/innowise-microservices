package com.innowise.orderservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemCreateDto(
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 3, max = 64, message = "Name must be between 3 and 64 characters")
        String name,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.00", message = "Price must be non-negative")
        BigDecimal price
) {}
