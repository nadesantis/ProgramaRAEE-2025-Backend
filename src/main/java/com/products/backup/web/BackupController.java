package com.products.backup.web;

import com.products.backup.application.BackupOptions;
import com.products.backup.application.PdfBackupService;
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

  private final PdfBackupService pdfBackup;

  public BackupController(PdfBackupService pdfBackup) {
    this.pdfBackup = pdfBackup;
  }

  @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<byte[]> backupPdf(
      @RequestParam(defaultValue = "true") boolean products,
      @RequestParam(defaultValue = "true") boolean clients,
      @RequestParam(defaultValue = "true") boolean orders,
      @RequestParam(defaultValue = "true") boolean users,
      @RequestParam(defaultValue = "false") boolean audit,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant auditFrom,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant auditTo
  ) {

	  BackupOptions opt = new BackupOptions();
	  opt.setIncludeProducts(products);
	  opt.setIncludeClients(clients);
	  opt.setIncludeOrders(orders);
	  opt.setIncludeUsers(users);
	  opt.setIncludeAudit(audit);
	  opt.setAuditFrom(auditFrom);
	  opt.setAuditTo(auditTo);

	  byte[] pdf = pdfBackup.generateFullBackupPdf(opt);


    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        .replace(":", "-");
    String filename = "backup-" + ts + ".pdf";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.APPLICATION_PDF)
        .contentLength(pdf.length)
        .body(pdf);
  }
}
