package com.example.inventorymngt.ordermanagementSystem;

import com.example.inventorymngt.common.enums.OrderStatus;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OmsOrderServiceBehaviorTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OmsOrderService omsOrderService;

    @Test
    @DisplayName("Should return orders for a specific user sorted by recency")
    void getAllOrders_FilteredByUserId_ReturnsRecentOrdersOnly() {
        Order orderOne = Order.builder()
                .id(1L)
                .userId(42)
                .productName("Keyboard")
                .quantity(1)
                .unitPrice(new BigDecimal("49.99"))
                .totalAmount(new BigDecimal("49.99"))
                .status(OrderStatus.CREATED)
                .build();

        Order orderTwo = Order.builder()
                .id(2L)
                .userId(42)
                .productName("Mouse")
                .quantity(2)
                .unitPrice(new BigDecimal("20.00"))
                .totalAmount(new BigDecimal("40.00"))
                .status(OrderStatus.CANCELLED)
                .build();

        Mockito.when(orderRepository.findByUserIdOrderByCreatedAtDesc(42)).thenReturn(List.of(orderTwo, orderOne));

        List<OrderResponseDTO> response = omsOrderService.getAllOrders(42);

        assertEquals(2, response.size());
        assertEquals(2L, response.get(0).getId());
        assertEquals(42, response.get(0).getUserId());
        assertEquals("Mouse", response.get(0).getProductName());
    }

    @Test
    @DisplayName("Should return a single order response with snapshot details")
    void getOrderById_ReturnsSnapshotDetails() {
        Order historicOrder = Order.builder()
                .id(7L)
                .userId(77)
                .productName("Legacy Headset")
                .quantity(3)
                .unitPrice(new BigDecimal("15.00"))
                .totalAmount(new BigDecimal("45.00"))
                .status(OrderStatus.CREATED)
                .build();

        Mockito.when(orderRepository.findById(7L)).thenReturn(Optional.of(historicOrder));

        OrderResponseDTO response = omsOrderService.getOrderById(7L);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("Legacy Headset", response.getProductName());
        assertEquals(3, response.getQuantity());
        assertEquals(new BigDecimal("45.00"), response.getTotalAmount());
    }
}
