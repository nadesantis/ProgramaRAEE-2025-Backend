package com.products.orders.infrastructure.repository;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;
import com.products.orders.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderJpaRepository spring;

    public JpaOrderRepository(SpringDataOrderJpaRepository spring) {
        this.spring = spring;
    }

    @Override
    public Order save(Order order) { 
        return spring.save(order); 
    }

    @Override
    public Optional<Order> findById(Long id) { 
        return spring.findById(id); 
    }

    @Override
    public Page<Order> findAll(Pageable pageable) { 
        return spring.findAll(pageable); 
    }

    // ✅ Agregá este método para exportar todo sin paginar
    @Override
    public List<Order> findAll() { 
        return spring.findAll(); 
    }

    @Override
    public Page<Order> findByClientId(Long clientId, Pageable pageable) { 
        return spring.findByClientId(clientId, pageable); 
    }

    @Override
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) { 
        return spring.findByStatus(status, pageable); 
    }

    @Override
    public Page<Order> findByClientIdAndStatus(Long clientId, OrderStatus status, Pageable pageable) {
        return spring.findByClientIdAndStatus(clientId, status, pageable);
    }

    @Override
    public Page<Order> findByClientIdIn(Collection<Long> clientIds, Pageable pageable) {
        return spring.findByClientIdIn(clientIds, pageable);
    }

    @Override
    public Page<Order> findByClientIdInAndStatus(Collection<Long> clientIds, OrderStatus status, Pageable pageable) {
        return spring.findByClientIdInAndStatus(clientIds, status, pageable);
    }
}
