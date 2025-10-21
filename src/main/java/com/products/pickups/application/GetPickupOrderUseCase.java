package com.products.pickups.application;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetPickupOrderUseCase {
  private final PickupOrderRepository repo;
  public GetPickupOrderUseCase(PickupOrderRepository repo) { this.repo = repo; }
  public Optional<PickupOrder> handle(Long id) { return repo.findById(id); }
}

