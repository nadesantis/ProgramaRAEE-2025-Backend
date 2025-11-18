// src/main/java/com/products/pickups/web/dto/PickupOrderListRow.java
package com.products.pickups.web.dto;

import com.products.pickups.domain.PickupStatus;
import java.time.Instant;

public class PickupOrderListRow {
  private Long id;
  private String clientName;
  private String technicianName;
  private PickupStatus status;
  private Instant requestedAt;
  private String location;
  private Double raeeKg;   // <— antes BigDecimal

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getClientName() { return clientName; }
  public void setClientName(String clientName) { this.clientName = clientName; }
  public String getTechnicianName() { return technicianName; }
  public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
  public PickupStatus getStatus() { return status; }
  public void setStatus(PickupStatus status) { this.status = status; }
  public Instant getRequestedAt() { return requestedAt; }
  public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
  public Double getRaeeKg() { return raeeKg; }
  public void setRaeeKg(Double raeeKg) { this.raeeKg = raeeKg; }
}
