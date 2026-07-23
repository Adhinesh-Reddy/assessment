package com.example.inventorymngt.orderManagementSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inventorymngt.orderManagementSystem.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Fetch order history grouped by specific users
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
}