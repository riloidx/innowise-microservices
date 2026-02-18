package com.innowise.authservice.service.api;

import com.innowise.authservice.dto.request.UserCreateDto;
import com.innowise.authservice.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${integration.user-service-url}")
public interface UserFeignClient {

    @PostMapping("/api/users")
    UserResponseDto createUser(@RequestBody UserCreateDto dto);

    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
