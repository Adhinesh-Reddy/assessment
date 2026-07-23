package com.example.inventorymngt.ordermanagementSystem;

import com.example.inventorymngt.common.enums.OrderStatus;
import com.example.inventorymngt.common.enums.ProductCategory;
import com.example.inventorymngt.exception.InvalidOrderStateException;
import com.example.inventorymngt.exception.ResourceNotFoundException;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.orderManagementSystem.dto.OrderRequestDTO;
import com.example.inventorymngt.orderManagementSystem.dto.OrderResponseDTO;
import com.example.inventorymngt.orderManagementSystem.entity.Order;
import com.example.inventorymngt.orderManagementSystem.repository.OrderRepository;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OmsCriticalScenariosUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OmsOrderService omsOrderService;

    @Test
    @DisplayName("Should reject ordering when a product is soft-deleted")
    void placeOrder_SoftDeletedProduct_ThrowsException() {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(101)
                .productId(5L)
                .quantity(1)
                .build();

        // The repository query automatically returns empty for soft-deleted items
        Mockito.when(productRepository.findActiveByIdForUpdate(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> omsOrderService.placeOrder(request));
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
    }

    @Test
    @DisplayName("Should successfully retrieve old order history even if the referenced product was soft-deleted")
    void getOrderById_ReferencedProductSoftDeleted_Success() {
        // A soft-deleted product relationship becomes null or missing in the database entity lifecycle[cite: 1]
        Order historicOrder = Order.builder()
                .id(99L)
                .userId(101)
                .product(null) // Product relationship is gone[cite: 1]
                .productName("Legacy Laptop") // Snapshot fields preserve accurate details[cite: 1]
                .unitPrice(new BigDecimal("899.99"))
                .quantity(1)
                .totalAmount(new BigDecimal("899.99"))
                .status(OrderStatus.CREATED)
                .build();

        Mockito.when(orderRepository.findById(99L)).thenReturn(Optional.of(historicOrder));

        OrderResponseDTO response = omsOrderService.getOrderById(99L);

        assertNotNull(response);
        assertNull(response.getProductId());
        assertEquals("Legacy Laptop", response.getProductName()); // Verified immutable snapshot data[cite: 1]
        assertEquals(new BigDecimal("899.99"), response.getUnitPrice());
    }

    @Test
    @DisplayName("Should reject order cancellation if the order status is already CANCELLED")
    void cancelOrder_AlreadyCancelled_ThrowsException() {
        Order alreadyCancelledOrder = Order.builder()
                .id(42L)
                .status(OrderStatus.CANCELLED) // State machine conflict[cite: 1]
                .build();

        Mockito.when(orderRepository.findById(42L)).thenReturn(Optional.of(alreadyCancelledOrder));

        // State machine rules state only CREATED orders can be cancelled[cite: 1]
        assertThrows(InvalidOrderStateException.class, () -> omsOrderService.cancelOrder(42L));
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }
}