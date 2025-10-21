// src/main/java/com/products/audit/domain/AuditLogRepositoryPort.java
package com.products.audit.domain;

import com.products.audit.domain.AuditAction;
import com.products.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepositoryPort extends JpaRepository<AuditLog, Long> {

  List<AuditLog> findByWhenAtBetween(Instant from, Instant to);

  Page<AuditLog> findByUserEmailContainingIgnoreCase(String userEmail, Pageable pageable);

  Page<AuditLog> findByAction(AuditAction action, Pageable pageable);


  @Query("""
         select a
         from AuditLog a
         where (:user is null or lower(a.userEmail) like lower(concat('%', :user, '%')))
           and (:action is null or a.action = :action)
         """)
  Page<AuditLog> search(@Param("user") String user,
                        @Param("action") AuditAction action,
                        Pageable pageable);
}
