package com.example.inventorymngt.inventoryManagementSystem.service;

import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.exception.ResourceNotFoundException;
import com.example.inventorymngt.inventoryManagementSystem.dto.ProductRequestDTO;
import com.example.inventorymngt.inventoryManagementSystem.dto.ProductResponseDTO;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImsProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO addProduct(ProductRequestDTO dto) {
        log.info("[IMS] Creating new product: {}", dto.getName());
        
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(ProductCategory.fromString(dto.getCategory()))
                .price(dto.getPrice())
                .stock(0) // New products explicitly start with zero stock
                .isDeleted(false)
                .build();

        Product saved = productRepository.save(product);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        log.info("[IMS] Updating details for Product ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(ProductCategory.fromString(dto.getCategory()));
        product.setPrice(dto.getPrice());
        // Stock field is bypassed to block structural overrides during basic profile edits

        return mapToResponseDTO(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("[IMS] Executing soft-delete tracking check for Product ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Soft delete enables historical foreign key integrity on past order transactions
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponseDTO adjustStock(Long id, Integer amount) {
        log.info("[IMS] Adjusting inventory balance for Product ID: {} | Delta: {}", id, amount);
        
        // Employs pessimistic write lock to secure mutations from concurrent transaction overrides
        Product product = productRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with ID: " + id));

        int updatedStock = product.getStock() + amount;
        if (updatedStock < 0) {
            log.error("[IMS] Rejection: Adjusting stock level by {} would drive total below zero.", amount);
            throw new IllegalArgumentException("Inventory stock level cannot be adjusted below zero.");
        }
        if (updatedStock > 999999) {
            log.error("[IMS] Rejection: Adjusting stock level by {} would drive total maximum limit of 999,999.", amount);
            throw new IllegalArgumentException("Inventory stock level cannot exceed the maximum limit of 999,999.");
        }

        product.setStock(updatedStock);
        return mapToResponseDTO(productRepository.save(product));
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory().name())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}