package com.products.pickups.infrastructure.repository;

import com.products.orders.domain.Order;
import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupOrderRepository;
import com.products.pickups.domain.PickupStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaPickupOrderRepository implements PickupOrderRepository {

	@Autowired
  private  SpringDataPickupJpaRepository spring;

  public JpaPickupOrderRepository(SpringDataPickupJpaRepository spring) {
    this.spring = spring;
  }

  @Override public PickupOrder save(PickupOrder o) { return spring.save(o); }
  @Override public Optional<PickupOrder> findById(Long id) { return spring.findById(id); }
  @Override public Page<PickupOrder> findAll(Pageable pageable) { return spring.findAll(pageable); }
  @Override public Page<PickupOrder> findByClientId(Long clientId, Pageable pageable) { return spring.findByClientId(clientId, pageable); }
  @Override public Page<PickupOrder> findByTechnicianId(Long techId, Pageable pageable) { return spring.findByTechnicianId(techId, pageable); }
  @Override public Page<PickupOrder> findByStatus(PickupStatus status, Pageable pageable) { return spring.findByStatus(status, pageable); }
  @Override public Page<PickupOrder> findByClientIdAndStatus(Long clientId, PickupStatus status, Pageable pageable) { return spring.findByClientIdAndStatus(clientId, status, pageable); }
  @Override public Page<PickupOrder> findByTechnicianIdAndStatus(Long techId, PickupStatus status, Pageable pageable) { return spring.findByTechnicianIdAndStatus(techId, status, pageable); }
  @Override public void delete(PickupOrder o) { spring.delete(o); }

@Override
public List<PickupOrder> findAll() {
	return spring.findAll();
}
	


}
