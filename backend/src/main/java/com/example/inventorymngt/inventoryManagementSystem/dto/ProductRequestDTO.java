package com.example.inventorymngt.inventoryManagementSystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required.")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters.")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;

    @NotBlank(message = "Category is required.")
    @Pattern(regexp = "^(Electronics|Clothing|Home & Garden|Sports|Books|Other)$", message = "Category must be one of the allowed values.")
    private String category;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.00", message = "Price must be zero or positive.")
    @DecimalMax(value = "99999.99", message = "Price cannot exceed 99,999.99.")
    @Digits(integer = 5, fraction = 2, message = "Price must be formatted with up to 2 decimal places.")
    private BigDecimal price;
}