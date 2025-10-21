package com.products.pickups.application;

import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;
import com.products.pickups.domain.PickupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListPickupOrdersUseCase {
  private final PickupOrderRepository repo;
  public ListPickupOrdersUseCase(PickupOrderRepository repo) { this.repo = repo; }

  public Page<PickupOrder> handle(Long clientId, Long technicianId, PickupStatus status, Pageable pageable) {
    if (clientId != null && technicianId != null && status != null) {
      return repo.findByTechnicianIdAndStatus(technicianId, status, pageable);
    } else if (technicianId != null && status != null) {
      return repo.findByTechnicianIdAndStatus(technicianId, status, pageable);
    } else if (clientId != null && status != null) {
      return repo.findByClientIdAndStatus(clientId, status, pageable);
    } else if (technicianId != null) {
      return repo.findByTechnicianId(technicianId, pageable);
    } else if (clientId != null) {
      return repo.findByClientId(clientId, pageable);
    } else if (status != null) {
      return repo.findByStatus(status, pageable);
    }
    return repo.findAll(pageable);
  }
}
