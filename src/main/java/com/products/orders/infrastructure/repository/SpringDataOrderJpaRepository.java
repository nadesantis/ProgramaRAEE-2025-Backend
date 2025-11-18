// src/main/java/com/products/orders/infrastructure/repository/SpringDataOrderJpaRepository.java
package com.products.orders.infrastructure.repository;

import com.products.orders.domain.Order;
import com.products.orders.domain.OrderStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

  Page<Order> findByClientIdIn(Collection<Long> clientIds, Pageable pageable);

  Page<Order> findByClientIdInAndStatus(Collection<Long> clientIds, OrderStatus status, Pageable pageable);

  // ✅ importante: cuando traemos por ID, vengan los items cargados
  @Override
  @EntityGraph(attributePaths = "items")
  Optional<Order> findById(Long id);
}
