package com.products.pickups.web;

import java.time.Instant;

public class ClosePickupRequest {
  /** Fecha/hora de cierre (ISO). Si viene null, se usa now(). */
  private Instant closedAt;
  /** Duración total (minutos) */
  private Integer durationMinutes;
  /** Cantidad de equipos retirados */
  private Integer devicesCount;
  /** Comentarios */
  private String notes;

  public Instant getClosedAt() { return closedAt; }
  public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
  public Integer getDurationMinutes() { return durationMinutes; }
  public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
  public Integer getDevicesCount() { return devicesCount; }
  public void setDevicesCount(Integer devicesCount) { this.devicesCount = devicesCount; }
  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }
}
