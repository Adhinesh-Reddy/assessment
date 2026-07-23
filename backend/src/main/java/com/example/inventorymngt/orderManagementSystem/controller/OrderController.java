package com.example.inventorymngt.orderManagementSystem.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.inventorymngt.orderManagementSystem.dto.OrderRequestDTO;
import com.example.inventorymngt.orderManagementSystem.dto.OrderResponseDTO;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OmsOrderService omsOrderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody OrderRequestDTO payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(omsOrderService.placeOrder(payload));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listAllOrders(@RequestParam(required = false) Integer userId) {
        return ResponseEntity.ok(omsOrderService.getAllOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderDetails(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(omsOrderService.getOrderById(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(omsOrderService.cancelOrder(id));
    }
}