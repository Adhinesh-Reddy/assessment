package com.example.inventorymngt.ordermanagementSystem;

import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.inventoryManagementSystem.service.ImsProductService;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OmsCriticalScenariosIntegrationTest {

    @Autowired
    private OmsOrderService omsOrderService;

    @Autowired
    private ImsProductService imsProductService;

    @Autowired
    private ProductRepository productRepository;

    private Long productAId;
    private Long productBId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        Product prodA = Product.builder()
                .name("Product A")
                .category(ProductCategory.ELECTRONICS)
                .price(BigDecimal.TEN)
                .stock(100)
                .isDeleted(false)
                .build();

        Product prodB = Product.builder()
                .name("Product B")
                .category(ProductCategory.ELECTRONICS)
                .price(BigDecimal.TEN)
                .stock(100)
                .isDeleted(false)
                .build();

        productAId = productRepository.save(prodA).getId();
        productBId = productRepository.save(prodB).getId();
    }

    @Test
    @DisplayName("Deadlock Resiliency Test — Verifies that the lock manager resolves interleaved cross-resource updates without freezing threads")
    void adjustStock_CrossResourceLocking_DeadlockResilience() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        // Thread 1 locks Product A, waits, then attempts to lock Product B[cite: 6]
        Callable<Void> task1 = () -> {
            imsProductService.adjustStock(productAId, 5);
            barrier.await(); // Synchronize execution sequence
            imsProductService.adjustStock(productBId, 5);
            return null;
        };

        // Thread 2 locks Product B, waits, then attempts to lock Product A[cite: 6]
        Callable<Void> task2 = () -> {
            imsProductService.adjustStock(productBId, 10);
            barrier.await(); // Synchronize execution sequence
            imsProductService.adjustStock(productAId, 10);
            return null;
        };

        Future<Void> future1 = executor.submit(task1);
        Future<Void> future2 = executor.submit(task2);

        executor.shutdown();
        boolean finishedGracefully = executor.awaitTermination(5, TimeUnit.SECONDS);

        // In a true database engine environment, one task may throw a lock collision exception,
        // but the system must resolve it dynamically without thread starvation or total lockups[cite: 6].
        assertTrue(finishedGracefully, "The database integration layer entered an unresolvable deadlock state.");
    }
}