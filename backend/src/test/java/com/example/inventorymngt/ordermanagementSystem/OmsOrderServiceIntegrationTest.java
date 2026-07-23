package com.example.inventorymngt.ordermanagementSystem;

import com.example.inventorymngt.BackendApplication;
import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.exception.InsufficientStockException;
import com.example.inventorymngt.exception.ResourceNotFoundException;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.orderManagementSystem.dto.OrderRequestDTO;
import com.example.inventorymngt.orderManagementSystem.dto.OrderResponseDTO;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OmsOrderServiceIntegrationTest {

    @Autowired
    private OmsOrderService omsOrderService;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        testProduct = Product.builder()
                .name("Wireless Mouse")
                .description("Ergonomic mouse")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("29.99"))
                .stock(10)
                .isDeleted(false)
                .build();
        
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("TC-OMS-03: Should throw InsufficientStockException and roll back when stock level is insufficient")
    void placeOrder_InsufficientInventory() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(789)
                .productId(testProduct.getId())
                .quantity(11) // Exceeds available stock (10)
                .build();

        assertThrows(InsufficientStockException.class, () -> omsOrderService.placeOrder(request));

        // Verify transaction integrity: stock remains unchanged
        Product postFailedOrderProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(10, postFailedOrderProduct.getStock());
    }

    @Test
    @DisplayName("TC-OMS-04: Should throw ResourceNotFoundException when product ID is invalid")
    void placeOrder_InvalidProductId() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(789)
                .productId(9999L) // Non-existent ID
                .quantity(1)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> omsOrderService.placeOrder(request));
    }

    @Test
    @DisplayName("TC-OMS-05: High Concurrency Stress Test — Handles simultaneous product checkouts safely")
    void placeOrder_ConcurrentRaceConditions() throws InterruptedException, ExecutionException {
        Long productId = testProduct.getId();
        int totalThreads = 8;
        
        // Setting quantity to 2 per request means 8 requests need 16 units total.
        // Since stock is 10, exactly 5 requests must succeed and 3 must fail.
        OrderRequestDTO concurrentRequest = OrderRequestDTO.builder()
                .userId(999)
                .productId(productId)
                .quantity(2)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        List<Callable<OrderResponseDTO>> tasks = new ArrayList<>();

        for (int i = 0; i < totalThreads; i++) {
            tasks.add(() -> omsOrderService.placeOrder(concurrentRequest));
        }

        // Execute all checkout requests simultaneously
        List<Future<OrderResponseDTO>> futures = executorService.invokeAll(tasks);
        executorService.shutdown();

        int successfulOrders = 0;
        int failedOrders = 0;

        for (Future<OrderResponseDTO> future : futures) {
            try {
                OrderResponseDTO res = future.get();
                if (res != null) successfulOrders++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof InsufficientStockException) {
                    failedOrders++;
                } else {
                    fail("Unexpected failure exception occurred: " + e.getCause());
                }
            }
        }

        // Assert serialization constraints met exactly
        assertEquals(5, successfulOrders, "Exactly 5 checkout placements should succeed");
        assertEquals(3, failedOrders, "Exactly 3 checkouts should be blocked due to insufficient stock");

        // Assert remaining base stock matches perfectly
        Product finalizedProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(0, finalizedProduct.getStock(), "Final item stock level must drop exactly to zero");
    }
}