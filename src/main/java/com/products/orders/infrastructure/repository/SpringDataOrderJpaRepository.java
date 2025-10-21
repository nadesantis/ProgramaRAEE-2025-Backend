package com.products.orders.infrastructure.repository;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderJpaRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByClientId(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByClientIdAndStatus(Long clientId, OrderStatus status, Pageable pageable);
}
