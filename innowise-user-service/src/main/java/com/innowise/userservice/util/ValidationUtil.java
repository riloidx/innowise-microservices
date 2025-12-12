package com.innowise.userservice.util;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {
    public void validateMatchingIds(long pathId, long dtoId) {
        if (pathId != dtoId) {
            throw new ValidationException("pathId and dtoId must match");
        }
    }
}
