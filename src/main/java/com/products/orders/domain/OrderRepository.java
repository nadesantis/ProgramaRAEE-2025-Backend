package com.products.orders.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);

    Page<Order> findAll(Pageable pageable);
    Page<Order> findByClientId(Long clientId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByClientIdAndStatus(Long clientId, OrderStatus status, Pageable pageable);

    Page<Order> findByClientIdIn(Collection<Long> clientIds, Pageable pageable);

    Page<Order> findByClientIdInAndStatus(Collection<Long> clientIds, OrderStatus status, Pageable pageable);
	List<Order> findAll();
}
