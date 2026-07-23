package com.example.inventorymngt.common.enums;

public enum ProductCategory {
    ELECTRONICS,
    CLOTHING,
    HOME_GARDEN,
    SPORTS,
    BOOKS,
    OTHER;

    public static ProductCategory fromString(String value) {
        try {
            return ProductCategory.valueOf(value.toUpperCase().replace(" & ", "_"));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid product category provided.");
        }
    }
}