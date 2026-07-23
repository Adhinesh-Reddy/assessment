package com.example.inventorymngt.orderManagementSystem.service;

import com.example.inventorymngt.common.enums.OrderStatus;
import com.example.inventorymngt.exception.InsufficientStockException;
import com.example.inventorymngt.exception.InvalidOrderStateException;
import com.example.inventorymngt.exception.ResourceNotFoundException;
import com.example.inventorymngt.inventoryManagementSystem.entity.Product;
import com.example.inventorymngt.inventoryManagementSystem.repository.ProductRepository;
import com.example.inventorymngt.orderManagementSystem.dto.OrderRequestDTO;
import com.example.inventorymngt.orderManagementSystem.dto.OrderResponseDTO;
import com.example.inventorymngt.orderManagementSystem.dto.StorefrontProductResponseDTO;
import com.example.inventorymngt.orderManagementSystem.entity.Order;
import com.example.inventorymngt.orderManagementSystem.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsOrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<StorefrontProductResponseDTO> getStorefrontCatalog() {
        // Enforces visibility constraints automatically filtering out soft-deleted items
        return productRepository.findAllActiveProducts().stream()
                .map(p -> StorefrontProductResponseDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .category(p.getCategory().name())
                        .price(p.getPrice())
                        .inStock(p.getStock() > 0) // Exact numeric counts remain fully hidden
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StorefrontProductResponseDTO getStorefrontProductById(Long id) {
        Product p = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not available or out of catalog bounds."));
        
        return StorefrontProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .category(p.getCategory().name())
                .price(p.getPrice())
                .inStock(p.getStock() > 0)
                .build();
    }

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO dto) {
        log.info("[OMS] Order creation requested by User ID: {} for Product ID: {} | Quantity: {}", 
                dto.getUserId(), dto.getProductId(), dto.getQuantity());

        // Pessimistic Write Lock isolates product row immediately to prevent concurrent over-purchasing race conditions
        Product product = productRepository.findActiveByIdForUpdate(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Target product is unavailable or does not exist."));

        if (product.getStock() < dto.getQuantity()) {
            log.warn("[OMS] Checkout rejected: Insufficient stock for Product ID: {}. Requested: {}, Available: {}", 
                    product.getId(), dto.getQuantity(), product.getStock());
            throw new InsufficientStockException("Insufficient stock available to complete this order.");
        }

        // Deduct inventory allocation balance from active stock levels
        product.setStock(product.getStock() - dto.getQuantity());
        productRepository.save(product);

        // Calculate monetary totals using high-precision decimal math
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        // Capture immutable snapshot information at transaction time
        Order order = Order.builder()
                .userId(dto.getUserId())
                .product(product)
                .productName(product.getName()) // Freezes historical item name snapshot
                .quantity(dto.getQuantity())
                .unitPrice(product.getPrice())   // Freezes historical pricing snapshot
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("[OMS] Order ID: {} successfully generated.", savedOrder.getId());
        
        return mapToOrderResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return getAllOrders(null);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders(Integer userId) {
        List<Order> orders = userId == null
                ? orderRepository.findAll()
                : orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order records not found for Reference ID: " + id));
        return mapToOrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long id) {
        log.info("[OMS] Cancellation process initialized for Order ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order records not found for Reference ID: " + id));

        // State machine integrity guard assertion
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Only active orders matching state [CREATED] can be cancelled.");
        }

        if (order.getProduct() != null) {
            Product product = productRepository.findActiveByIdForUpdate(order.getProduct().getId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + order.getQuantity());
                productRepository.save(product);
            } else {
                log.warn("[OMS] Order ID: {} is linked to a product that is no longer active. Stock restoration skipped.", id);
            }
        } else {
            log.warn("[OMS] Order ID: {} relation link is null. Stock restoration step bypassed.", id);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        log.info("[OMS] Order ID: {} has been marked as CANCELLED.", updatedOrder.getId());

        return mapToOrderResponseDTO(updatedOrder);
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProduct() != null ? order.getProduct().getId() : null)
                .productName(order.getProductName()) // Returns snapshot details directly
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}