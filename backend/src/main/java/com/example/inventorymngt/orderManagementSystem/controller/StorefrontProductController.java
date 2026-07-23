package com.example.inventorymngt.orderManagementSystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.inventorymngt.orderManagementSystem.dto.StorefrontProductResponseDTO;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/store/products")
@RequiredArgsConstructor
public class StorefrontProductController {

    private final OmsOrderService omsOrderService;

    @GetMapping
    public ResponseEntity<List<StorefrontProductResponseDTO>> getStorefrontCatalog() {
        return ResponseEntity.ok(omsOrderService.getStorefrontCatalog());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorefrontProductResponseDTO> getStorefrontProductDetails(@PathVariable Long id) {
        return ResponseEntity.ok(omsOrderService.getStorefrontProductById(id));
    }
}