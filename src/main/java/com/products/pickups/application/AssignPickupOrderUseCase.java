package com.products.pickups.application;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignPickupOrderUseCase {

	@Autowired
  private  PickupOrderRepository repo;

  public AssignPickupOrderUseCase(PickupOrderRepository repo) { this.repo = repo; }

  @Transactional
  public PickupOrder handle(Long id, Long technicianId) {
    PickupOrder o = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    o.assign(technicianId);
    return repo.save(o);
  }
}
