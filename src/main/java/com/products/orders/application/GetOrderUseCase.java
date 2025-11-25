package com.products.orders.application;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetOrderUseCase {

    private final OrderRepository repo;

    public GetOrderUseCase(OrderRepository repo) { this.repo = repo; }

    public Optional<Order> handle(Long id) { return repo.findById(id); }
}
