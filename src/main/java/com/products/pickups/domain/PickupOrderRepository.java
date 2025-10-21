package com.products.pickups.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PickupOrderRepository {
  PickupOrder save(PickupOrder o);
  Optional<PickupOrder> findById(Long id);
  Page<PickupOrder> findAll(Pageable pageable);
  Page<PickupOrder> findByClientId(Long clientId, Pageable pageable);
  Page<PickupOrder> findByTechnicianId(Long techId, Pageable pageable);
  Page<PickupOrder> findByStatus(PickupStatus status, Pageable pageable);
  Page<PickupOrder> findByClientIdAndStatus(Long clientId, PickupStatus status, Pageable pageable);
  Page<PickupOrder> findByTechnicianIdAndStatus(Long techId, PickupStatus status, Pageable pageable);
}
