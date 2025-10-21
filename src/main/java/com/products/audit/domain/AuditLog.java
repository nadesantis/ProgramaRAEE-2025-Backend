// src/main/java/com/products/audit/domain/AuditLog.java
package com.products.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_when",   columnList = "whenAt"),
        @Index(name = "idx_audit_user",   columnList = "userEmail"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity", columnList = "entityName,entityId")
    }
)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Cuándo ocurrió */
  @Column(nullable = false)
  private Instant whenAt = Instant.now();

  /** Quién */
  @Column(length = 180)
  private String userEmail;

  /** Roles del usuario en CSV simple (ej: "ADMIN,OPERADOR_VENTAS") */
  @Column(length = 400)
  private String roles;

  /** Desde dónde */
  @Column(length = 60)
  private String ip;

  /** Request (opcional) */
  @Column(length = 8)
  private String method;

  @Column(length = 300)
  private String path;

  /** Qué acción */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private AuditAction action = AuditAction.GENERIC;

  /** Entidad afectada */
  @Column(length = 120)
  private String entityName;

  private Long entityId;

  /** Resultado */
  @Column(nullable = false)
  private boolean success = true;

  @Column(length = 1024)
  private String message;

  /** Campo libre para JSON/extra */
  @Lob
  private String metadata;

  private Long durationMs;

  // ---------- Ciclo de vida ----------
  @PrePersist
  public void prePersist() {
    if (whenAt == null) whenAt = Instant.now();
  }

  // ---------- Getters/Setters ----------
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Instant getWhenAt() { return whenAt; }
  public void setWhenAt(Instant whenAt) { this.whenAt = whenAt; }

  public String getUserEmail() { return userEmail; }
  public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

  public String getRoles() { return roles; }
  public void setRoles(String roles) { this.roles = roles; }

  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }

  public String getMethod() { return method; }
  public void setMethod(String method) { this.method = method; }

  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }

  public AuditAction getAction() { return action; }
  public void setAction(AuditAction action) { this.action = action; }

  public String getEntityName() { return entityName; }
  public void setEntityName(String entityName) { this.entityName = entityName; }

  public Long getEntityId() { return entityId; }
  public void setEntityId(Long entityId) { this.entityId = entityId; }

  public boolean isSuccess() { return success; }
  public void setSuccess(boolean success) { this.success = success; }

  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }

  public String getMetadata() { return metadata; }
  public void setMetadata(String metadata) { this.metadata = metadata; }

  public Long getDurationMs() { return durationMs; }
  public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

  public Instant getCreatedAt() { return getWhenAt(); }


  public String getUsername() { return getUserEmail(); }

 
  public String getEntityType() { return getEntityName(); }

 
  public String getResult() { return success ? "SUCCESS" : "FAILURE"; }
}
