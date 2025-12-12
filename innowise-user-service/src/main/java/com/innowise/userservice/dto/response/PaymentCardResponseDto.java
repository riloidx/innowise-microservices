package com.innowise.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCardResponseDto {
    private long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private boolean active;
}
