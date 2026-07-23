package com.example.inventorymngt.orderManagementSystem.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class StorefrontProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private boolean inStock; // Exposes state without leaking quantitative counts
}