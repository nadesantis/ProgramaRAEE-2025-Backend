package com.products.audit.application;

import com.products.audit.domain.AuditAction;
import com.products.audit.domain.AuditLog;
import com.products.audit.support.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import com.products.audit.infrastructure.AuditLogRepository;

@Service
public class AuditService {
  private final AuditLogRepository repo;
  private final HttpServletRequest request; 

  public AuditService(AuditLogRepository repo, HttpServletRequest request) {
    this.repo = repo;
    this.request = request;
  }

  public void success(AuditAction action, String entity, Long entityId, String message) {
    save(true, action, entity, entityId, message, null, null);
  }

  public void failure(AuditAction action, String entity, Long entityId, String message) {
    save(false, action, entity, entityId, message, null, null);
  }

  public void success(AuditAction action, String message) {
    save(true, action, null, null, message, null, null);
  }

  public void failure(AuditAction action, String message) {
    save(false, action, null, null, message, null, null);
  }

  public void save(boolean ok,
                   AuditAction action,
                   String entity,
                   Long entityId,
                   String message,
                   String metadataJson,
                   Long durationMs) {
    AuditLog log = new AuditLog();
    log.setSuccess(ok);
    log.setAction(action);
    log.setEntityName(entity);
    log.setEntityId(entityId);
    log.setMessage(message);
    log.setMetadata(metadataJson);
    log.setDurationMs(durationMs);

    log.setUserEmail(CurrentUser.email());
    log.setRoles(CurrentUser.rolesCsv());

    if (request != null) {
      log.setIp(CurrentUser.clientIp(request));
      log.setMethod(request.getMethod());
      log.setPath(request.getRequestURI());
    }

    repo.save(log);
  }
}
