package com.products.pickups.infrastructure.repository;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPickupJpaRepository extends JpaRepository<PickupOrder, Long> {
  @EntityGraph(attributePaths = {}) 
  Page<PickupOrder> findByClientId(Long clientId, Pageable pageable);

  @EntityGraph(attributePaths = {})
  Page<PickupOrder> findByTechnicianId(Long technicianId, Pageable pageable);

  @EntityGraph(attributePaths = {})
  Page<PickupOrder> findByStatus(PickupStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {})
  Page<PickupOrder> findByClientIdAndStatus(Long clientId, PickupStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {})
  Page<PickupOrder> findByTechnicianIdAndStatus(Long technicianId, PickupStatus status, Pageable pageable);
}
