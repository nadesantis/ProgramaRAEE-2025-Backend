package com.products.orders.application;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApproveOrderUseCase {

    private final OrderRepository orderRepo;

    public ApproveOrderUseCase(OrderRepository orderRepo) { this.orderRepo = orderRepo; }

    @Transactional
    public Order handle(Long orderId) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        o.approve();
        return orderRepo.save(o);
    }
}
