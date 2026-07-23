package com.example.inventorymngt.ordermanagementSystem;

import com.example.inventorymngt.common.enums.OrderStatus;
import com.example.inventorymngt.orderManagementSystem.dto.OrderRequestDTO;
import com.example.inventorymngt.orderManagementSystem.dto.OrderResponseDTO;
import com.example.inventorymngt.orderManagementSystem.service.OmsOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.example.inventorymngt.orderManagementSystem.controller.OrderController.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OmsOrderService omsOrderService;

    @Test
    @DisplayName("TC-OMS-01: Should create order successfully when payload is valid")
    void placeOrder_Success() throws Exception {
        OrderRequestDTO request = OrderRequestDTO.builder()
                .userId(123)
                .productId(1L)
                .quantity(2)
                .build();

        OrderResponseDTO response = OrderResponseDTO.builder()
                .id(50L)
                .userId(123)
                .productId(1L)
                .productName("Mechanical Keyboard")
                .quantity(2)
                .unitPrice(new BigDecimal("89.99"))
                .totalAmount(new BigDecimal("179.98"))
                .status(OrderStatus.CREATED.name())
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(omsOrderService.placeOrder(Mockito.any(OrderRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.productName").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.totalAmount").value(179.98))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("TC-OMS-02: Should return 400 Bad Request when quantity breaks lower constraints")
    void placeOrder_ValidationError_LowQuantity() throws Exception {
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(123)
                .productId(1L)
                .quantity(0) // Minimum is 1
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    @DisplayName("TC-OMS-02b: Should return 400 Bad Request when quantity breaks upper constraints")
    void placeOrder_ValidationError_HighQuantity() throws Exception {
        OrderRequestDTO invalidRequest = OrderRequestDTO.builder()
                .userId(123)
                .productId(1L)
                .quantity(1000) // Maximum allowed is 999
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    @DisplayName("TC-OMS-06: Should reject invalid order IDs in the path")
    void getOrderDetails_InvalidPathId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.id").exists());
    }

    @Test
    @DisplayName("TC-OMS-07: Should list user scoped orders when a userId filter is supplied")
    void listOrders_WithUserFilter_ReturnsFilteredCollection() throws Exception {
        Mockito.when(omsOrderService.getAllOrders(42)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/orders").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$" ).isArray());
    }
}