package com.products.orders.application;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;
import com.products.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListOrdersUseCase {

    private final OrderRepository repo;

    public ListOrdersUseCase(OrderRepository repo) { this.repo = repo; }

    public Page<Order> handle(Long clientId, OrderStatus status, Pageable pageable) {
        if (clientId != null && status != null) {
            return repo.findByClientIdAndStatus(clientId, status, pageable);
        } else if (clientId != null) {
            return repo.findByClientId(clientId, pageable);
        } else if (status != null) {
            return repo.findByStatus(status, pageable);
        }
        return repo.findAll(pageable);
    }
}
