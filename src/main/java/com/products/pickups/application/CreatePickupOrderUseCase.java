package com.products.pickups.application;

import com.products.clients.domain.ClientRepository;
import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePickupOrderUseCase {

  private final PickupOrderRepository repo;
  private final ClientRepository clientRepo;

  public CreatePickupOrderUseCase(PickupOrderRepository repo, ClientRepository clientRepo) {
    this.repo = repo;
    this.clientRepo = clientRepo;
  }

//src/main/java/com/products/pickups/application/CreatePickupOrderUseCase.java
@Transactional
public PickupOrder handle(Long clientId, String location, String notes, java.math.BigDecimal raeeKg) {
 clientRepo.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Cliente no existe"));

 PickupOrder o = new PickupOrder();
 o.setClientId(clientId);
 o.setLocation(location);
 o.setNotes(notes);
 o.setRaeeKg(raeeKg != null ? raeeKg.doubleValue() : null); // <— conversión a Double

 return repo.save(o);
}

}

