package com.innowise.paymentservice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "random-number-integration", url = "${api.random-number-url}")
public interface RandomNumberClient {

    @GetMapping
    List<RandomNumberResponseDto> getRandomNumber();
}
