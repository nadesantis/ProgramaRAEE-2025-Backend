// src/main/java/com/products/backup/web/ExcelBackupController.java
package com.products.backup.web;

import com.products.backup.xlsx.ExcelBackupService;
import com.products.backup.xlsx.ImportResult;
import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;
import lombok.RequiredArgsConstructor;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/backup/excel")
@RequiredArgsConstructor
public class ExcelBackupController {
	@Autowired
  private ExcelBackupService service;
	@Autowired
  private AuditService audit;

  /** Exporta TODO (sin usuarios) a un XLSX 
  @GetMapping(produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<byte[]> exportAll() {
    long t0 = System.currentTimeMillis();
    byte[] xlsx = service.exportAll();
    long ms = System.currentTimeMillis() - t0;

    audit.success(AuditAction.GENERIC, "Backup", null,
        "Export XLSX ok [bytes=" + xlsx.length + "] [durationMs=" + ms + "]");

    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
    String filename = "backup-" + ts + ".xlsx";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(xlsx.length)
        .body(xlsx);
  }
  
  */
  
  
  
  /** ✅ EXPORTA TODO A EXCEL (productos + clientes + órdenes) */
  @GetMapping(produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<byte[]> exportAll() {
    long start = System.currentTimeMillis();

    byte[] xlsx = service.exportAll();
    long ms = System.currentTimeMillis() - start;

    audit.success(
        AuditAction.GENERIC,
        "Backup",
        null,
        "Export XLSX ok [bytes=" + xlsx.length + "] [durationMs=" + ms + "]"
    );

    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
    String filename = "backup-" + ts + ".xlsx";

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .contentLength(xlsx.length)
        .body(xlsx);
  }
  
  
  
  

  /** Importa Excel (opcional dryRun para probar sin grabar) */
  @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ImportResult> importAll(
      @RequestParam("file") MultipartFile file,
      @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun
  ) {
    try {
      long t0 = System.currentTimeMillis();
      ImportResult result = service.importAll(file.getInputStream(), dryRun);
      long ms = System.currentTimeMillis() - t0;

      audit.success(AuditAction.GENERIC, "Backup", null,
          "Import XLSX " + (dryRun ? "[dryRun]" : "") +
              " ok [createdOrUpdated=" + result.totalUpserts() +
              "] [errors=" + result.errors().size() +
              "] [durationMs=" + ms + "]");

      return ResponseEntity.ok(result);
    } catch (Exception ex) {
      audit.failure(AuditAction.GENERIC, "Backup", null,
          "Import XLSX error: " + String.valueOf(ex.getMessage()));
      throw new IllegalStateException("No se pudo importar XLSX", ex);
    }
  }
}
