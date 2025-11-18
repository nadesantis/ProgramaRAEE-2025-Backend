package com.products.pickups.application;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ClosePickupOrderUseCase {

	@Autowired
  private PickupOrderRepository repo;

  public ClosePickupOrderUseCase(PickupOrderRepository repo) { this.repo = repo; }

  @Transactional
  public PickupOrder handle(Long id,
                            Long technicianId,
                            Integer durationMinutes,
                            Integer devicesCount,
                            String notes) {

    PickupOrder o = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

    o.close(technicianId, Instant.now(), durationMinutes, devicesCount, notes); // <— firma completa
    return repo.save(o);
  }
}