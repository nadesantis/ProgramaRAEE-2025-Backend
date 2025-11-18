package com.products.backup.application;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Service
public class OrderPdfService {

  private static final Font H1 = new Font(Font.HELVETICA, 18, Font.BOLD);
  private static final Font H2 = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
  private static final Font TH = new Font(Font.HELVETICA, 10, Font.BOLD);
  private static final Font TD = new Font(Font.HELVETICA, 10, Font.NORMAL);

  private static final DateTimeFormatter DTF =
      DateTimeFormatter.ofPattern("dd/MM/uuuu, HH:mm").withZone(ZoneId.systemDefault());

  public byte[] build(Order o, String clientName, String clientTaxId) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      Document doc = new Document(PageSize.A4, 28, 28, 28, 28);
      PdfWriter.getInstance(doc, baos);
      doc.open();

      // Título
      Paragraph t = new Paragraph("Detalles de la Orden", H1);
      t.setSpacingAfter(12f);
      doc.add(t);

      // Meta (igual al modal)
      PdfPTable meta = new PdfPTable(2);
      meta.setWidthPercentage(100);
      meta.setSpacingAfter(10f);
      meta.addCell(cellBold("Cliente:"));     meta.addCell(cell(nd(clientName)));
      meta.addCell(cellBold("CUIT:"));        meta.addCell(cell(nd(clientTaxId)));
      meta.addCell(cellBold("Fecha:"));       meta.addCell(cell(o.getCreatedAt() == null ? "—" : DTF.format(o.getCreatedAt())));
      meta.addCell(cellBold("Estado:"));      meta.addCell(cell(statusEs(o.getStatus() == null ? null : o.getStatus().name())));
      doc.add(meta);

      // Items
      Paragraph subt = new Paragraph("Productos", H2);
      subt.setSpacingBefore(6f);
      subt.setSpacingAfter(6f);
      doc.add(subt);

      PdfPTable table = new PdfPTable(new float[]{48, 12, 20, 20});
      table.setWidthPercentage(100);
      addTh(table, "Producto"); addTh(table, "Cantidad"); addTh(table, "Precio"); addTh(table, "Subtotal");

      BigDecimal total = BigDecimal.ZERO;
      if (o.getItems() != null) {
        for (OrderItem it : o.getItems()) {
          BigDecimal unit = nz(it.getUnitPrice());
          BigDecimal qty  = BigDecimal.valueOf(it.getQuantity() == null ? 0 : it.getQuantity());
          BigDecimal line = it.getLineTotal() != null ? it.getLineTotal() : unit.multiply(qty);

          addTd(table, nd(it.getProductName() != null ? it.getProductName() : "#" + it.getProductId()));
          addTdNum(table, qty);
          addTdNum(table, unit);
          addTdNum(table, line);

          total = total.add(line);
        }
      }
      doc.add(table);

      // Total
      PdfPTable tot = new PdfPTable(new float[]{80, 20});
      tot.setWidthPercentage(100);
      PdfPCell l = new PdfPCell(new Phrase("Total:", TH));
      l.setBorder(Rectangle.NO_BORDER);
      l.setHorizontalAlignment(Element.ALIGN_RIGHT);
      l.setPaddingTop(8f);
      PdfPCell v = new PdfPCell(new Phrase(money(total), new Font(Font.HELVETICA, 12, Font.BOLD)));
      v.setBorder(Rectangle.NO_BORDER);
      v.setHorizontalAlignment(Element.ALIGN_RIGHT);
      v.setPaddingTop(8f);
      tot.addCell(l); tot.addCell(v);
      doc.add(tot);

      doc.close();
      return baos.toByteArray();
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo generar PDF de orden", e);
    }
  }

  // helpers
  private PdfPCell cellBold(String s){ PdfPCell c=new PdfPCell(new Phrase(s, H2)); c.setBorder(Rectangle.NO_BORDER); return c; }
  private PdfPCell cell(String s){ PdfPCell c=new PdfPCell(new Phrase(s, TD)); c.setBorder(Rectangle.NO_BORDER); return c; }
  private void addTh(PdfPTable t, String s){ PdfPCell c=new PdfPCell(new Phrase(s, TH)); c.setBackgroundColor(new Color(235,235,235)); c.setPadding(5); t.addCell(c); }
  private void addTd(PdfPTable t, String s){ PdfPCell c=new PdfPCell(new Phrase(s, TD)); c.setPadding(4); t.addCell(c); }
  private void addTdNum(PdfPTable t, BigDecimal n){ PdfPCell c=new PdfPCell(new Phrase(money(n), TD)); c.setHorizontalAlignment(Element.ALIGN_RIGHT); c.setPadding(4); t.addCell(c); }
  private BigDecimal nz(BigDecimal v){ return v==null ? BigDecimal.ZERO : v; }
  private String nd(String s){ return (s==null||s.isBlank()) ? "—" : s; }
  private String money(BigDecimal v){ return "$ " + v.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString(); }
  private String statusEs(String s){
    if (s==null) return "—";
    return switch (s) { case "PENDING","CREATED" -> "Pendiente"; case "APPROVED" -> "Aprobado"; case "CLOSED" -> "Cerrada"; default -> s; };
  }
}
