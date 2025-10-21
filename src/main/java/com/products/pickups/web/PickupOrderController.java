package com.products.pickups.web;

import com.products.pickups.application.*;
import com.products.pickups.domain.PickupOrder;
import com.products.pickups.domain.PickupStatus;
import com.products.pickups.web.dto.*;
import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

  private final UserRepository userRepo;

  public PickupOrderController(CreatePickupOrderUseCase createUC,
                               ApprovePickupOrderUseCase approveUC,
                               AssignPickupOrderUseCase assignUC,
                               StartPickupOrderUseCase startUC,
                               ClosePickupOrderUseCase closeUC,
                               GetPickupOrderUseCase getUC,
                               ListPickupOrdersUseCase listUC,
                               UserRepository userRepo) {
    this.createUC = createUC;
    this.approveUC = approveUC;
    this.assignUC = assignUC;
    this.startUC = startUC;
    this.closeUC = closeUC;
    this.getUC = getUC;
    this.listUC = listUC;
    this.userRepo = userRepo;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> create(@Valid @RequestBody CreatePickupRequest req) {
    PickupOrder o = createUC.handle(req.getClientId(), req.getLocation(), req.getNotes());
    return ResponseEntity.ok(toResponse(o));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> approve(@PathVariable Long id) {
    PickupOrder o = approveUC.handle(id);
    return ResponseEntity.ok(toResponse(o));
  }

  @PostMapping("/{id}/assign")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO')")
  public ResponseEntity<PickupOrderResponse> assign(@PathVariable Long id, @Valid @RequestBody AssignPickupRequest req) {
    PickupOrder o = assignUC.handle(id, req.getTechnicianId());
    return ResponseEntity.ok(toResponse(o));
  }

  @PostMapping("/{id}/start")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PickupOrderResponse> start(@PathVariable Long id, Authentication auth) {
    Long technicianId = extractUserId(auth); // ahora busca por email -> id
    PickupOrder o = startUC.handle(id, technicianId);
    return ResponseEntity.ok(toResponse(o));
  }

  @PostMapping("/{id}/close")
  @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
  public ResponseEntity<PickupOrderResponse> close(@PathVariable Long id,
                                                   @RequestBody ClosePickupRequest req,
                                                   Authentication auth) {
    Long technicianId = extractUserId(auth);
    PickupOrder o = closeUC.handle(id, technicianId, req == null ? null : req.getNotes());
    return ResponseEntity.ok(toResponse(o));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO','TECNICO')")
  public ResponseEntity<PickupOrderResponse> get(@PathVariable Long id, Authentication auth) {
    var opt = getUC.handle(id);
    if (opt.isEmpty()) {
      return ResponseEntity.status(404).<PickupOrderResponse>build();
    }

    var o = opt.get();
    if (!canSee(o, auth)) {
      return ResponseEntity.status(403).<PickupOrderResponse>build();
    }

    return ResponseEntity.ok(toResponse(o));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_LOGISTICO','TECNICO')")
  public Page<PickupOrderResponse> list(@RequestParam(required = false) Long clientId,
                                        @RequestParam(required = false) PickupStatus status,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        Authentication auth) {
    Pageable pageable = PageRequest.of(page, size);
    Long techFilter = isTech(auth) ? extractUserId(auth) : null;
    return listUC.handle(clientId, techFilter, status, pageable).map(this::toResponse);
  }

  // ===== helpers =====
  private boolean isTech(Authentication auth) {
    return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO"));
  }

  private boolean canSee(PickupOrder o, Authentication auth) {
    if (auth == null) return false;
    boolean adminOrOpLog =
        auth.getAuthorities().stream().anyMatch(a ->
            a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OPERADOR_LOGISTICO"));
    if (adminOrOpLog) return true;
    if (isTech(auth)) {
      Long uid = extractUserId(auth);
      return o.getTechnicianId() != null && o.getTechnicianId().equals(uid);
    }
    return false;
  }

  private Long extractUserId(Authentication auth) {
    if (auth == null) return null;
    String email = auth.getName(); 
    if (email == null || email.isBlank()) return null;

    return userRepo.findByEmail(email)
        .map(User::getId)
        .orElse(null);
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
    return r;
  }
}
