package com.example.inventorymngt.inventoryManagementSystem;

import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.inventoryManagementSystem.service.ImsProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
class ImsInventoryIntegrationTest {

    @Autowired
    private ImsProductService imsProductService;

    @Autowired
    private ProductRepository productRepository;

    private Long sharedProductId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        Product item = Product.builder()
                .name("High Contention Processing Units")
                .category(ProductCategory.ELECTRONICS)
                .price(new BigDecimal("499.99"))
                .stock(500) // Initial stock pool balance[cite: 1]
                .isDeleted(false)
                .build();

        item = productRepository.save(item);
        sharedProductId = item.getId();
    }

    @Test
    @DisplayName("High Concurrency Stress Test — Verifies parallel back-office restock requests serialize safely without data anomalies")
    void adjustStock_ConcurrentInventoryUpdates() throws InterruptedException {
        int parallelWorkerThreads = 10;
        int incrementDeltaPerExecution = 50; 
        // 10 worker threads performing adjustments of +50 units simultaneously = expected delta of +500 units[cite: 1]

        ExecutorService executorService = Executors.newFixedThreadPool(parallelWorkerThreads);
        CountDownLatch executionSynchronizerLatch = new CountDownLatch(1);
        CountDownLatch completionTrackerLatch = new CountDownLatch(parallelWorkerThreads);

        for (int i = 0; i < parallelWorkerThreads; i++) {
            executorService.submit(() -> {
                try {
                    executionSynchronizerLatch.await(); // Hold threads to execute simultaneously
                    imsProductService.adjustStock(sharedProductId, incrementDeltaPerExecution);
                } catch (Exception e) {
                    fail("Concurrency execution step crashed due to transaction lock failure: " + e.getMessage());
                } finally {
                    completionTrackerLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        executionSynchronizerLatch.countDown();
        boolean finishedGracefully = completionTrackerLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertTrue(finishedGracefully, "Parallel stock adjustments timed out.");

        // Pull fresh non-stale record from the database to evaluate lock commitments
        Product postConcurrencyProduct = productRepository.findById(sharedProductId).orElseThrow();
        
        // Initial stock (500) + (10 executions × 50 units) must equal exactly 1000 units[cite: 1]
        assertEquals(1000, postConcurrencyProduct.getStock(), 
                "Concurrent operations dropped or corrupted update deltas due to race conditions.");
    }
}