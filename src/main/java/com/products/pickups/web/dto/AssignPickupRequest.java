package com.products.pickups.web.dto;

import jakarta.validation.constraints.NotNull;

public class AssignPickupRequest {
  @NotNull private Long technicianId;
  public Long getTechnicianId() { return technicianId; }
  public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
}
