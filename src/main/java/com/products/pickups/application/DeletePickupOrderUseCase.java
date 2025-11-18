package com.products.pickups.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;
import com.products.pickups.domain.PickupStatus;
import jakarta.transaction.Transactional;

@Service
public class DeletePickupOrderUseCase {

  @Autowired
  private PickupOrderRepository repo;

  public DeletePickupOrderUseCase() {
    // Constructor vacío necesario para Spring sin Lombok
  }

  @Transactional
  public void handle(Long id) {
    PickupOrder o = repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

    if (o.getStatus() != PickupStatus.CREATED) {
      throw new IllegalStateException("Sólo se puede eliminar en estado CREATED");
    }

    repo.delete(o);
  }
}
