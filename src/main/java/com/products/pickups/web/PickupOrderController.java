package com.products.pickups.web;

import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import com.products.pickups.application.*;
import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupStatus;
import com.products.pickups.web.dto.*;
import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;

import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pickups")
public class PickupOrderController {

  private final CreatePickupOrderUseCase createUC;
  private final ApprovePickupOrderUseCase approveUC;
  private final AssignPickupOrderUseCase assignUC;
  private final StartPickupOrderUseCase startUC;
  private final ClosePickupOrderUseCase closeUC;
  private final GetPickupOrderUseCase getUC;
  private final ListPickupOrdersUseCase listUC;
  private final DeletePickupOrderUseCase deleteUC;

  private final UserRepository userRepo;
  private final ClientRepository clientRepo;
  private final AuditService audit; // ⬅️ auditoría

  public PickupOrderController(CreatePickupOrderUseCase createUC,
                               ApprovePickupOrderUseCase approveUC,
                               AssignPickupOrderUseCase assignUC,
                               StartPickupOrderUseCase startUC,
                               ClosePickupOrderUseCase closeUC,
                               GetPickupOrderUseCase getUC,
                               ListPickupOrdersUseCase listUC,
                               DeletePickupOrderUseCase deleteUC,
                               UserRepository userRepo,
                               ClientRepository clientRepo,
                               AuditService audit) {
    this.createUC = createUC;
    this.approveUC = approveUC;
    this.assignUC = assignUC;
    this.startUC = startUC;
    this.closeUC = closeUC;
    this.getUC = getUC;
    this.listUC = listUC;
    this.deleteUC = deleteUC;
    this.userRepo = userRepo;
    this.clientRepo = clientRepo;
    this.audit = audit;
  }

  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> create(@Valid @RequestBody CreatePickupRequest req) {
    try {
      PickupOrder o = createUC.handle(req.getClientId(), req.getLocation(), req.getNotes(), req.getRaeeKg());
      audit.success(AuditAction.GENERIC, "PickupOrder", o.getId(),
          "Creada" + meta("clientId", req.getClientId()) + meta("kg", req.getRaeeKg()));
      return ResponseEntity.ok(toResponse(o));
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "Error al crear PickupOrder: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  @GetMapping("/technicians")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public List<IdNameDTO> listTechnicians() {
    var list = userRepo.findTechnicians()
        .stream()
        .map(u -> new IdNameDTO(u.getId(), u.getName()))
        .toList();
    audit.success(AuditAction.USER_LIST, "Listado de técnicos");
    return list;
  }

  // Obtener una orden
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO','TECNICO')")
  public ResponseEntity<PickupOrderResponse> get(@PathVariable Long id) {
    return getUC.handle(id)
        .map(po -> {
          audit.success(AuditAction.GENERIC, "PickupOrder", id, "Detalle");
          return ResponseEntity.ok(toResponse(po));
        })
        .orElseGet(() -> {
          audit.failure(AuditAction.GENERIC, "PickupOrder", id, "No encontrada");
          return ResponseEntity.notFound().build();
        });
  }

  // Asignar técnico
  @PostMapping("/{id}/assign")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> assign(@PathVariable Long id,
                                                    @Valid @RequestBody AssignPickupRequest req) {
    try {
      var o = assignUC.handle(id, req.getTechnicianId());
      audit.success(AuditAction.GENERIC, "PickupOrder", id, "Asignada a técnico " + req.getTechnicianId());
      return ResponseEntity.ok(toResponse(o));
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "PickupOrder", id, "Error al asignar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  // Iniciar
  @PostMapping("/{id}/start")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PickupOrderResponse> start(@PathVariable Long id, Authentication auth) {
    Long techId = extractUserId(auth);
    try {
      var o = startUC.handle(id, techId);
      audit.success(AuditAction.GENERIC, "PickupOrder", id, "Iniciada por " + techId);
      return ResponseEntity.ok(toResponse(o));
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "PickupOrder", id, "Error al iniciar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  // Cerrar
  @PostMapping("/{id}/close")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PickupOrderResponse> close(@PathVariable Long id,
                                                   @RequestBody(required = false) ClosePickupRequest req,
                                                   Authentication auth) {
    Long technicianId = extractUserId(auth);
    Integer duration = (req != null) ? req.getDurationMinutes() : null;
    Integer devices  = (req != null) ? req.getDevicesCount()     : null;
    String  notes    = (req != null) ? req.getNotes()            : null;
    try {
      PickupOrder o = closeUC.handle(id, technicianId, duration, devices, notes);
      audit.success(AuditAction.GENERIC, "PickupOrder", id,
          "Cerrada por " + technicianId + meta("durationMin", duration) + meta("devices", devices));
      return ResponseEntity.ok(toResponse(o));
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "PickupOrder", id, "Error al cerrar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> approve(@PathVariable Long id) {
    try {
      PickupOrder o = approveUC.handle(id);
      audit.success(AuditAction.GENERIC, "PickupOrder", id, "Aprobada");
      return ResponseEntity.ok(toResponse(o));
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "PickupOrder", id, "Error al aprobar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    try {
      deleteUC.handle(id);
      audit.success(AuditAction.GENERIC, "PickupOrder", id, "Eliminada");
      return ResponseEntity.noContent().build();
    } catch (RuntimeException ex) {
      audit.failure(AuditAction.GENERIC, "PickupOrder", id, "Error al eliminar: " + safe(ex.getMessage()));
      throw ex;
    }
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO','TECNICO')")
  public Page<PickupOrderListRow> list(@RequestParam(required = false) PickupStatus status,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Authentication auth) {
    Pageable pageable = PageRequest.of(page, size);
    Long techFilter = isTech(auth) ? extractUserId(auth) : null;

    // caches para evitar N+1
    Map<Long, String> clientNameCache = new HashMap<>();
    Map<Long, String> techNameCache = new HashMap<>();

    var result = listUC.handle(techFilter, status, pageable).map(o -> {
      PickupOrderListRow r = new PickupOrderListRow();
      r.setId(o.getId());
      // cliente
      String cName = "—";
      if (o.getClientId() != null) {
        cName = clientNameCache.computeIfAbsent(o.getClientId(),
            id -> clientRepo.findById(id).map(Client::getName).orElse("—"));
      }
      r.setClientName(cName);

      // técnico
      String tName = "—";
      if (o.getTechnicianId() != null) {
        tName = techNameCache.computeIfAbsent(o.getTechnicianId(),
            id -> userRepo.findById(id).map(User::getName).orElse("—"));
      }
      r.setTechnicianName(tName);

      r.setStatus(o.getStatus());
      r.setRequestedAt(o.getRequestedAt());
      r.setLocation(o.getLocation());
      r.setRaeeKg(o.getRaeeKg());
      return r;
    });

    audit.success(AuditAction.GENERIC, "Listado de pickups" + meta("status", status) + meta("techFilter", techFilter));
    return result;
  }

  // helpers
  private boolean isTech(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"));
  }

  private Long extractUserId(Authentication auth) {
    if (auth == null) return null;
    String email = auth.getName();
    if (email == null || email.isBlank()) return null;
    return userRepo.findByEmail(email).map(User::getId).orElse(null);
  }

  private PickupOrderResponse toResponse(PickupOrder o) {
    PickupOrderResponse r = new PickupOrderResponse();
    r.setId(o.getId());
    r.setClientId(o.getClientId());
    r.setTechnicianId(o.getTechnicianId());
    r.setStatus(o.getStatus());
    r.setLocation(o.getLocation());
    r.setNotes(o.getNotes());
    r.setRequestedAt(o.getRequestedAt());
    r.setApprovedAt(o.getApprovedAt());
    r.setAssignedAt(o.getAssignedAt());
    r.setStartedAt(o.getStartedAt());
    r.setClosedAt(o.getClosedAt());
    r.setCloseDurationMinutes(o.getCloseDurationMinutes());
    r.setDevicesCount(o.getDevicesCount());
    r.setRaeeKg(o.getRaeeKg());
    return r;
  }

  /** Metadatos simples clave=valor para mensajes de auditoría (sin JSON). */
  private String meta(String key, Object value) {
    return value == null ? "" : " [" + key + "=" + value + "]";
  }

  private String safe(String s) {
    return (s == null || s.isBlank()) ? "unknown" : s;
  }
}
