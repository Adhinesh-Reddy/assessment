package com.example.inventorymngt.inventoryManagementSystem.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.inventorymngt.inventoryManagementSystem.dto.ProductRequestDTO;
import com.example.inventorymngt.inventoryManagementSystem.dto.ProductResponseDTO;
import com.example.inventorymngt.inventoryManagementSystem.service.ImsProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class InternalProductController {

    private final ImsProductService imsProductService;

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listAllProducts() {
        return ResponseEntity.ok(imsProductService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductDetails(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(imsProductService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(@Valid @RequestBody ProductRequestDTO payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imsProductService.addProduct(payload));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable @Positive Long id, @Valid @RequestBody ProductRequestDTO payload) {
        return ResponseEntity.ok(imsProductService.updateProduct(id, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @Positive Long id) {
        imsProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDTO> adjustStock(
            @PathVariable @Positive Long id,
            @RequestParam @NotNull(message = "Amount is required.")
            @Min(value = -999999, message = "Amount must not be less than -999,999.")
            @Max(value = 999999, message = "Amount must not exceed 999,999.") Integer amount) {
        return ResponseEntity.ok(imsProductService.adjustStock(id, amount));
    }
}