package com.example.inventorymngt.orderManagementSystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull(message = "User ID is required.")
    @Positive(message = "User ID must be a positive number.")
    private Integer userId;

    @NotNull(message = "Product ID is required.")
    @Positive(message = "Product ID must be a positive number.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    @Max(value = 999, message = "Quantity cannot exceed 999.")
    private Integer quantity;
}