package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.request.ItemCreateDto;
import com.innowise.orderservice.dto.request.ItemUpdateDto;
import com.innowise.orderservice.dto.response.ItemResponseDto;
import com.innowise.orderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> create(@RequestBody @Valid ItemCreateDto itemCreateDto) {
        ItemResponseDto response = itemService.create(itemCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> findAll() {
        List<ItemResponseDto> response = itemService.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> findById(@PathVariable Long id) {
        ItemResponseDto response = itemService.findDtoById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ItemResponseDto> findByName(@RequestParam String name) {
        ItemResponseDto response = itemService.findByName(name);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid ItemUpdateDto itemUpdateDto
    ) {
        ItemResponseDto response = itemService.update(id, itemUpdateDto);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}