
package com.products.pickups.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pickup_orders", indexes = {
    @Index(name = "idx_pickup_client", columnList = "client_id"),
    @Index(name = "idx_pickup_tech", columnList = "technician_id"),
    @Index(name = "idx_pickup_status", columnList = "status")
})
public class PickupOrder {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "client_id", nullable = false)
  private Long clientId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 24, nullable = false)
  private PickupStatus status = PickupStatus.CREATED;

  @Column(name = "technician_id")
  private Long technicianId;

  @Column(name = "requested_at", updatable = false, nullable = false)
  private Instant requestedAt;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "assigned_at")
  private Instant assignedAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "closed_at")
  private Instant closedAt;

  @Column(name = "location", length = 240)
  private String location;

  @Column(name = "notes", length = 1000)
  private String notes;

  // NUEVOS: métricas de cierre
  @Column(name = "close_duration_minutes")
  private Integer closeDurationMinutes;

  @Column(name = "devices_count")
  private Integer devicesCount;

  @Column(name = "raee_kg")
  private Double raeeKg;

  @PrePersist void prePersist() {
    if (requestedAt == null) requestedAt = Instant.now();
    if (status == null) status = PickupStatus.CREATED;
  }

  public void approve() {
    if (status != PickupStatus.CREATED) throw new IllegalStateException("Solo CREATED puede aprobarse");
    status = PickupStatus.APPROVED;
    approvedAt = Instant.now();
  }

  public void assign(Long technicianId) {
    if (status != PickupStatus.APPROVED) throw new IllegalStateException("Debe estar APPROVED para asignar");
    if (technicianId == null) throw new IllegalArgumentException("technicianId requerido");
    this.technicianId = technicianId;
    status = PickupStatus.ASSIGNED;
    assignedAt = Instant.now();
  }

  public void start(Long whoId) {
    if (status != PickupStatus.ASSIGNED) throw new IllegalStateException("Debe estar ASSIGNED para iniciar");
    if (technicianId == null || !technicianId.equals(whoId)) {
      throw new IllegalStateException("Solo el técnico asignado puede iniciar");
    }
    status = PickupStatus.IN_PROGRESS;
    startedAt = Instant.now();
  }

  /** Cierre con campos enriquecidos */
  public void close(Long whoId, Instant closedAtInput, Integer durationMinutes,
                    Integer devicesCount, String closeNotes) {
    if (status != PickupStatus.IN_PROGRESS && status != PickupStatus.ASSIGNED) {
      throw new IllegalStateException("Solo ASSIGNED/IN_PROGRESS pueden cerrarse");
    }
    if (technicianId == null || !technicianId.equals(whoId)) {
      throw new IllegalStateException("Solo el técnico asignado puede cerrar");
    }
    status = PickupStatus.CLOSED;
    this.closedAt = (closedAtInput != null ? closedAtInput : Instant.now());
    this.closeDurationMinutes = durationMinutes;
    this.devicesCount = devicesCount;
    if (closeNotes != null && !closeNotes.isBlank()) {
      this.notes = (this.notes == null || this.notes.isBlank()) ? closeNotes : this.notes + " | " + closeNotes;
    }
  }

  // getters/setters …
  public Long getId() { return id; }
  public Long getClientId() { return clientId; }
  public PickupStatus getStatus() { return status; }
  public Long getTechnicianId() { return technicianId; }
  public Instant getRequestedAt() { return requestedAt; }
  public Instant getApprovedAt() { return approvedAt; }
  public Instant getAssignedAt() { return assignedAt; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getClosedAt() { return closedAt; }
  public String getLocation() { return location; }
  public String getNotes() { return notes; }
  public Integer getCloseDurationMinutes() { return closeDurationMinutes; }
  public Integer getDevicesCount() { return devicesCount; }
  public Double getRaeeKg() { return raeeKg; }

  public void setId(Long id) { this.id = id; }
  public void setClientId(Long clientId) { this.clientId = clientId; }
  public void setLocation(String location) { this.location = location; }
  public void setNotes(String notes) { this.notes = notes; }
  public void setRaeeKg(Double raeeKg) { this.raeeKg = raeeKg; }
}
