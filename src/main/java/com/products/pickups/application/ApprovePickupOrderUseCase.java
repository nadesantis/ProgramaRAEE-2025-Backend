package com.products.pickups.application;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovePickupOrderUseCase {

	@Autowired
  private  PickupOrderRepository repo;

  public ApprovePickupOrderUseCase(PickupOrderRepository repo) { this.repo = repo; }

  @Transactional
  public PickupOrder handle(Long id) {
    PickupOrder o = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
    o.approve();
    return repo.save(o);
  }
}
