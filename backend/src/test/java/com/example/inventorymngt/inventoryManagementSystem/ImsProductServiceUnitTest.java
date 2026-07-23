package com.example.inventorymngt.inventoryManagementSystem;

import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.exception.ResourceNotFoundException;
import com.example.inventorymngt.inventoryManagementSystem.dto.ProductRequestDTO;
import com.example.inventorymngt.inventoryManagementSystem.dto.ProductResponseDTO;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.inventoryManagementSystem.service.ImsProductService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImsProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ImsProductService imsProductService;

    @Test
    @DisplayName("Should initialize product with zero stock upon creation")
    void addProduct_InitializesZeroStock() {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Office Desk")
                .description("Wooden desk")
                .category("HOME_GARDEN")
                .price(new BigDecimal("250.00"))
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Office Desk")
                .description("Wooden desk")
                .category(ProductCategory.HOME_GARDEN)
                .price(new BigDecimal("250.00"))
                .stock(0) // Evaluates business rule constraint[cite: 1]
                .build();

        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(savedProduct);

        ProductResponseDTO response = imsProductService.addProduct(request);

        assertNotNull(response);
        assertEquals(0, response.getStock()); // Verifies mandatory initial baseline value[cite: 1]
    }

    @Test
@DisplayName("Should successfully increase inventory stock")
void adjustStock_Increase_Success() {
    // FIX: Include category and basic metadata in the builder
    Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .category(ProductCategory.ELECTRONICS)
            .price(BigDecimal.TEN)
            .stock(10)
            .isDeleted(false)
            .build();
            
    Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));
    Mockito.when(productRepository.save(Mockito.any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductResponseDTO response = imsProductService.adjustStock(1L, 5);

    assertEquals(15, response.getStock());
}

@Test
@DisplayName("Should successfully decrease inventory stock")
void adjustStock_Decrease_Success() {
    // FIX: Include category and basic metadata in the builder
    Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .category(ProductCategory.ELECTRONICS)
            .price(BigDecimal.TEN)
            .stock(10)
            .isDeleted(false)
            .build();
            
    Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));
    Mockito.when(productRepository.save(Mockito.any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductResponseDTO response = imsProductService.adjustStock(1L, -4);

    assertEquals(6, response.getStock());
}

@Test
@DisplayName("Should allow inventory stock level to fall exactly to zero")
void adjustStock_ReachesExactlyZero_Success() {
    // FIX: Include category and basic metadata in the builder
    Product product = Product.builder()
            .id(1L)
            .name("Test Product")
            .category(ProductCategory.ELECTRONICS)
            .price(BigDecimal.TEN)
            .stock(5)
            .isDeleted(false)
            .build();
            
    Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));
    Mockito.when(productRepository.save(Mockito.any(Product.class))).thenAnswer(i -> i.getArgument(0));

    ProductResponseDTO response = imsProductService.adjustStock(1L, -5);

    assertEquals(0, response.getStock());
}

    @Test
    @DisplayName("Should throw Exception when stock adjustment drives balance below zero")
    void adjustStock_DrivesBelowZero_ThrowsException() {
        Product product = Product.builder().id(1L).stock(5).isDeleted(false).build();
        Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));

        // Attempting to deduct 6 from 5 pushes stock to -1[cite: 1]
        assertThrows(IllegalArgumentException.class, () -> imsProductService.adjustStock(1L, -6));
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class)); // Verifies transactional block
    }

    @Test
    @DisplayName("Should throw Exception when stock adjustment exceeds the maximum limit")
    void adjustStock_ExceedsMaxLimit_ThrowsException() {
        // FIX: Include category and basic metadata so it passes any mapping validation path
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .category(ProductCategory.ELECTRONICS)
                .price(BigDecimal.TEN)
                .stock(999990)
                .isDeleted(false)
                .build();
                
        Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(product));

        // This will now cleanly hit your upper bound condition check and throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> imsProductService.adjustStock(1L, 10));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product target is missing or disabled")
    void adjustStock_ProductNotFound_ThrowsException() {
        Mockito.when(productRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> imsProductService.adjustStock(1L, 10));
    }

    @Test
    @DisplayName("Should bubble up DataIntegrityViolationException on underlying repository errors")
    void addProduct_RepositoryFailure_BubblesException() {
        Mockito.when(productRepository.save(Mockito.any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("Database unique name key constraint conflict."));

        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Conflict Name")
                .category("BOOKS")
                .price(BigDecimal.TEN)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> imsProductService.addProduct(request));
    }
}