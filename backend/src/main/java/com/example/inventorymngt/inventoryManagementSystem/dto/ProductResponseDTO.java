package com.example.inventorymngt.inventoryManagementSystem.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stock;
}