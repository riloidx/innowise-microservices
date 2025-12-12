package com.innowise.userservice.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    @Builder(builderMethodName = "validationBuilder")
    public ValidationErrorResponse(Instant timestamp, int status, String error, String message, String path,
                                   Map<String, String> validationErrors) {
        super(timestamp, status, error, message, path);
        this.validationErrors = validationErrors;
    }
}
