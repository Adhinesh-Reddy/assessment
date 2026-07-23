package com.example.inventorymngt.orderManagementSystem.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OrderResponseDTO {
    private Long id;
    private Integer userId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}