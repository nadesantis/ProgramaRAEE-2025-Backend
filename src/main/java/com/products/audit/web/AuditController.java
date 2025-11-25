package com.products.audit.web;

import com.products.audit.domain.AuditAction;
import com.products.audit.domain.AuditLog;
import com.products.audit.infrastructure.AuditLogRepository;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

  private final AuditLogRepository repo;

  public AuditController(AuditLogRepository repo) { this.repo = repo; }

  @GetMapping
  public Page<AuditLog> list(
      @RequestParam(required = false) String user,
      @RequestParam(required = false) AuditAction action,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "whenAt"));

    if (user != null && !user.isBlank()) {
      return repo.findByUserEmailContainingIgnoreCase(user.trim(), pageable);
    } else if (action != null) {
      return repo.findByAction(action, pageable);
    }
    return repo.findAll(pageable);
  }
}
