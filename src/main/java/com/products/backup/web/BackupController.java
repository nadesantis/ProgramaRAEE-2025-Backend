package com.products.backup.web;

import com.products.backup.application.BackupOptions;
import com.products.backup.application.PdfBackupService;
import com.products.backup.xlsx.ExcelBackupService;
import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

	@Autowired
  private PdfBackupService pdfBackup;
	@Autowired
  private AuditService audit; // ⬅️ añadimos bitácora
  @Autowired
  private ExcelBackupService excelBackupService;

  public BackupController(PdfBackupService pdfBackup, AuditService audit, ExcelBackupService excelBackupService) {
    this.pdfBackup = pdfBackup;
    this.audit = audit;
    this.excelBackupService = excelBackupService;
  }
  


  @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<byte[]> backupPdf(
      @RequestParam(defaultValue = "true") boolean products,
      @RequestParam(defaultValue = "true") boolean clients,
      @RequestParam(defaultValue = "true") boolean orders,
      @RequestParam(defaultValue = "true") boolean users,
      @RequestParam(defaultValue = "false") boolean auditIncluded,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant auditFrom,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant auditTo
  ) {
    try {
      BackupOptions opt = new BackupOptions();
      opt.setIncludeProducts(products);
      opt.setIncludeClients(clients);
      opt.setIncludeOrders(orders);
      opt.setIncludeUsers(users);
      opt.setIncludeAudit(auditIncluded);
      opt.setAuditFrom(auditFrom);
      opt.setAuditTo(auditTo);

      long start = System.currentTimeMillis();
      byte[] pdf = pdfBackup.generateFullBackupPdf(opt);
      long duration = System.currentTimeMillis() - start;

      String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
      String filename = "backup-" + ts + ".pdf";

      // Registrar en bitácora
      audit.success(
          AuditAction.GENERIC,
          "Backup",
          null,
          "Backup PDF generado" +
              meta("products", products) +
              meta("clients", clients) +
              meta("orders", orders) +
              meta("users", users) +
              meta("audit", auditIncluded) +
              meta("durationMs", duration)
      );

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(pdf.length)
          .body(pdf);
    } catch (Exception ex) {
      audit.failure(
          AuditAction.GENERIC,
          "Backup",
          null,
          "Error al generar backup PDF: " + safe(ex.getMessage())
      );
      throw ex;
    }
  }

  // Helpers
  private String meta(String key, Object value) {
    return value == null ? "" : " [" + key + "=" + value + "]";
  }

  private String safe(String s) {
    return (s == null || s.isBlank()) ? "unknown" : s;
  }
}
