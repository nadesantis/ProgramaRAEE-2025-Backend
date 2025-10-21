package com.products.orders.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);

    Page<Order> findAll(Pageable pageable);
    Page<Order> findByClientId(Long clientId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByClientIdAndStatus(Long clientId, OrderStatus status, Pageable pageable);
}
