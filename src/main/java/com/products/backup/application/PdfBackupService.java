package com.products.backup.application;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;
import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.orders.domain.OrderRepository;
import com.products.security.domain.User;
import com.products.security.infrastructure.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.products.audit.domain.AuditLog;
import com.products.audit.domain.AuditLogRepositoryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PdfBackupService {

@Autowired
  private  ProductRepository productRepo;
@Autowired
  private  ClientRepository  clientRepo;
@Autowired
  private  OrderRepository   orderRepo;
@Autowired
  private  UserRepository    userRepo;

@Autowired
  private @Nullable AuditLogRepositoryPort auditRepo;

  private static final Font H1 = new Font(Font.HELVETICA, 16, Font.BOLD);
  private static final Font H2 = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
  private static final Font TH = new Font(Font.HELVETICA, 10, Font.BOLD);
  private static final Font TD = new Font(Font.HELVETICA, 10, Font.NORMAL);

  private static final DateTimeFormatter DTF =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

public byte[] generateFullBackupPdf(BackupOptions opt) {
  try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
    Document doc = new Document(PageSize.A4.rotate(), 24, 24, 28, 28);
    PdfWriter writer = PdfWriter.getInstance(doc, baos);

    writer.setPageEvent(new PdfPageEventHelper() {
      @Override public void onEndPage(PdfWriter w, Document d) {
        ColumnText.showTextAligned(
            w.getDirectContent(),
            Element.ALIGN_RIGHT,
            new Phrase("Página " + d.getPageNumber(), new Font(Font.HELVETICA, 8)),
            d.right() - 10, d.bottom() - 10, 0
        );
      }
    });

    doc.open();

    addTitle(doc, "ProductsApp - Backup PDF");
    addSubtitle(doc, "Generado: " + DTF.format(Instant.now()));

    if (opt.isIncludeProducts()) {
      addSectionTitle(doc, "Productos");
      var products = productRepo.findAll(Pageable.unpaged()).getContent();
      addProductsTable(doc, products);
    }

    if (opt.isIncludeClients()) {
      addSectionTitle(doc, "Clientes");
      var clients = clientRepo.findAll(Pageable.unpaged()).getContent();
      addClientsTable(doc, clients);
    }

    if (opt.isIncludeOrders()) {
      addSectionTitle(doc, "Órdenes");
      var orders = orderRepo.findAll(Pageable.unpaged()).getContent();
      addOrdersTable(doc, orders);
    }

    if (opt.isIncludeUsers()) {
      addSectionTitle(doc, "Usuarios");
      var users = userRepo.findAll(Pageable.unpaged()).getContent();
      addUsersTable(doc, users);
    }

    if (opt.isIncludeAudit() && auditRepo != null) {
      addSectionTitle(doc, "Auditoría");
      List<AuditLog> logs =
          (opt.getAuditFrom() != null || opt.getAuditTo() != null)
              ? auditRepo.findByWhenAtBetween(
                    opt.getAuditFrom() != null ? opt.getAuditFrom() : Instant.EPOCH,
                    opt.getAuditTo()   != null ? opt.getAuditTo()   : Instant.now()
                )
              : auditRepo.findAll();
      addAuditTable(doc, logs);
    }

    doc.close();
    return baos.toByteArray();
  } catch (Exception e) {
    throw new IllegalStateException("Error generando PDF", e);
  }
}

  // ===== helpers =====

  private void addTitle(Document doc, String text) throws DocumentException {
    Paragraph p = new Paragraph(text, H1);
    p.setAlignment(Element.ALIGN_LEFT);
    p.setSpacingAfter(8f);
    doc.add(p);
  }

  private void addSubtitle(Document doc, String text) throws DocumentException {
    Paragraph p = new Paragraph(text, new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY));
    p.setAlignment(Element.ALIGN_LEFT);
    p.setSpacingAfter(16f);
    doc.add(p);
  }

  private void addSectionTitle(Document doc, String text) throws DocumentException {
    Paragraph p = new Paragraph(text, H2);
    p.setSpacingBefore(12f);
    p.setSpacingAfter(6f);
    doc.add(p);
  }

  private PdfPTable table(String... headers) {
    PdfPTable t = new PdfPTable(headers.length);
    t.setWidthPercentage(100);
    Stream.of(headers).forEach(h -> {
      PdfPCell c = new PdfPCell(new Phrase(h, TH));
      c.setBackgroundColor(new Color(235, 235, 235));
      c.setPadding(5);
      t.addCell(c);
    });
    return t;
  }

  private void addProductsTable(Document doc, List<Product> list) throws DocumentException {
    PdfPTable t = table("ID", "Nombre", "Descripción", "Precio", "Activo", "Creado");
    for (Product p : list) {
      addRow(t,
          str(p.getId()),
          nd(p.getName()),
          nd(p.getDescription()),
          str(p.getUnitPrice()),
          p.isActive() ? "Sí" : "No",
          fmt(p.getCreatedAt()));
    }
    doc.add(t);
  }

  private void addClientsTable(Document doc, List<Client> list) throws DocumentException {
    PdfPTable t = table("ID", "Nombre", "Email", "Teléfono", "CUIT", "Direcciones", "Creado");
    for (Client c : list) {
      String addrs = (c.getAddresses() == null || c.getAddresses().isEmpty())
          ? "—"
          : c.getAddresses().stream()
              .map(a -> join(", ", a.getStreet(), a.getCity(), a.getState(), a.getZip()))
              .reduce((a, b) -> a + " • " + b).orElse("—");
      addRow(t,
          str(c.getId()),
          nd(c.getName()),
          nd(c.getEmail()),
          nd(c.getPhone()),
          nd(c.getTaxId()),
          addrs,
          fmt(c.getCreatedAt()));
    }
    doc.add(t);
  }

  private void addOrdersTable(Document doc, List<Order> list) throws DocumentException {
    PdfPTable t = table("ID", "ClienteID", "Estado", "Total", "Fecha", "Items");
    for (Order o : list) {
      String items = (o.getItems() == null || o.getItems().isEmpty())
          ? "—"
          : o.getItems().stream().map(this::fmtItem).reduce((a, b) -> a + " | " + b).orElse("—");
      addRow(t,
          str(o.getId()),
          str(o.getClientId()),
          String.valueOf(o.getStatus()),
          str(o.getTotalAmount()),
          fmt(o.getCreatedAt()),
          items);
    }
    doc.add(t);
  }

  private String fmtItem(OrderItem it) {
    return nd(it.getProductName()) + " x" + str(it.getQuantity()) + " @" + str(it.getUnitPrice());
  }

  private void addUsersTable(Document doc, List<User> list) throws DocumentException {
    // Si tu User NO tiene createdAt, dejamos fuera la columna
    PdfPTable t = table("ID", "Nombre", "Email", "Roles", "Locked", "Intentos");
    for (User u : list) {
      String roles = (u.getRoles() == null || u.getRoles().isEmpty())
          ? "—"
          : u.getRoles().stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("—");
      addRow(t,
          str(u.getId()),
          nd(u.getName()),
          nd(u.getEmail()),
          roles,
          Boolean.TRUE.equals(u.getLocked()) ? "Sí" : "No",
          str(u.getFailedAttempts())
          // Si tu User SÍ tiene createdAt:
          // , fmt(u.getCreatedAt())
      );
    }
    doc.add(t);
  }

  private void addAuditTable(Document doc, List<AuditLog> list) throws DocumentException {
    PdfPTable t = table("ID", "Fecha", "Usuario", "Acción", "Entidad", "EntidadID", "Resultado", "Mensaje");
    for (AuditLog a : list) {
      addRow(t,
          str(a.getId()),
          fmt(a.getWhenAt()),
          nd(a.getUserEmail()),
          String.valueOf(a.getAction()),
          nd(a.getEntityName()),
          str(a.getEntityId()),
          a.isSuccess() ? "OK" : "FAIL",
          nd(a.getMessage()));
    }
    doc.add(t);
  }

  private void addRow(PdfPTable t, String... cols) {
    for (String v : cols) {
      PdfPCell c = new PdfPCell(new Phrase(v, TD));
      c.setPadding(4);
      t.addCell(c);
    }
  }

  private String str(Object o) { return o == null ? "—" : String.valueOf(o); }
  private String nd(String s)   { return (s == null || s.isBlank()) ? "—" : s; }
  private String join(String sep, String... parts) {
    return Stream.of(parts).filter(Objects::nonNull).filter(p -> !p.isBlank())
        .reduce((a,b)->a+sep+b).orElse("—");
  }
  private String fmt(Instant i) { return i == null ? "—" : DTF.format(i); }
}