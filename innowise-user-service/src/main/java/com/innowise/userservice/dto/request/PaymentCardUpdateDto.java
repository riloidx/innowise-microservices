package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardUpdateDto {

    @NotNull(message = "Card id is required")
    private Long id;

    @Size(min = 12, max = 64, message = "Card number length must be between 12 and 64 characters")
    private String number;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}
