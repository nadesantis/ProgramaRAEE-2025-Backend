// src/main/java/com/products/pickups/web/dto/PickupOrderResponse.java
package com.products.pickups.web.dto;

import com.products.pickups.domain.PickupStatus;
import java.time.Instant;

public class PickupOrderResponse {
  private Long id;
  private Long clientId;
  private Long technicianId;
  private PickupStatus status;

  private String location;
  private String notes;

  private Instant requestedAt;
  private Instant approvedAt;
  private Instant assignedAt;
  private Instant startedAt;
  private Instant closedAt;

  // métricas de cierre
  private Integer closeDurationMinutes;
  private Integer devicesCount;

  // raeeKg lo mantenemos como Double para coincidir con la entidad
  private Double raeeKg;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getClientId() { return clientId; }
  public void setClientId(Long clientId) { this.clientId = clientId; }

  public Long getTechnicianId() { return technicianId; }
  public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }

  public PickupStatus getStatus() { return status; }
  public void setStatus(PickupStatus status) { this.status = status; }

  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public Instant getRequestedAt() { return requestedAt; }
  public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

  public Instant getApprovedAt() { return approvedAt; }
  public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

  public Instant getAssignedAt() { return assignedAt; }
  public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

  public Instant getStartedAt() { return startedAt; }
  public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

  public Instant getClosedAt() { return closedAt; }
  public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

  public Integer getCloseDurationMinutes() { return closeDurationMinutes; }
  public void setCloseDurationMinutes(Integer closeDurationMinutes) { this.closeDurationMinutes = closeDurationMinutes; }

  public Integer getDevicesCount() { return devicesCount; }
  public void setDevicesCount(Integer devicesCount) { this.devicesCount = devicesCount; }

  public Double getRaeeKg() { return raeeKg; }
  public void setRaeeKg(Double raeeKg) { this.raeeKg = raeeKg; }
}
