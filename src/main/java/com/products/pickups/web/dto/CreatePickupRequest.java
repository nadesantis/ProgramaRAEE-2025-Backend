package com.products.pickups.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePickupRequest {
  @NotNull private Long clientId;
  @Size(max = 240) private String location;
  @Size(max = 1000) private String notes;
  private java.math.BigDecimal raeeKg; 

  public java.math.BigDecimal getRaeeKg() {
	return raeeKg;
}
public void setRaeeKg(java.math.BigDecimal raeeKg) {
	this.raeeKg = raeeKg;
}
public Long getClientId() { return clientId; }
  public void setClientId(Long clientId) { this.clientId = clientId; }
  public String getLocation() { return location; }
  public void setLocation(String location) { this.location = location; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
}
