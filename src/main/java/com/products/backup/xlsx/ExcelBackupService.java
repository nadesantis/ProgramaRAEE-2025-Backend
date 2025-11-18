package com.products.backup.xlsx;

import com.products.catalog.domain.Product;
import com.products.catalog.domain.ProductRepository;
import com.products.clients.domain.Client;
import com.products.clients.domain.ClientRepository;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelBackupService {

	@Autowired
    private ProductRepository productRepo;
	@Autowired
    private ClientRepository clientRepo;
	@Autowired
    private OrderRepository orderRepo;



    /**
     * Exporta todas las entidades (excepto usuarios) a un único archivo XLSX.
     */
    public byte[] exportAll() {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {

            exportProducts(wb);
            exportClients(wb);
            exportOrders(wb);

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Error al generar backup Excel", e);
        }
    }

    /**
     * Importa entidades desde un XLSX.
     * Si dryRun = true, solo analiza sin guardar.
     */
    public ImportResult importAll(InputStream is, boolean dryRun) {
        try (Workbook wb = new XSSFWorkbook(is)) {
            var result = new ImportResult(0, 0, 0, 0, new ArrayList<>());

            importProducts(wb, result, dryRun);
            importClients(wb, result, dryRun);

            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Error al importar Excel", e);
        }
    }

    // ========================================================
    // ============ EXPORTACIONES =============================
    // ========================================================

    private void exportProducts(Workbook wb) {
        Sheet sh = wb.createSheet("PRODUCTOS");
        Row header = sh.createRow(0);
        String[] cols = {"ID", "Nombre", "Descripción", "Precio", "Activo", "Creado", "Actualizado"};
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        var list = productRepo.findAll();
        int r = 1;
        for (Product p : list) {
            Row row = sh.createRow(r++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getDescription());
            row.createCell(3).setCellValue(p.getUnitPrice() != null ? p.getUnitPrice().doubleValue() : 0);
            row.createCell(4).setCellValue(p.isActive() ? "Sí" : "No");
            row.createCell(5).setCellValue(String.valueOf(p.getCreatedAt()));
            row.createCell(6).setCellValue(String.valueOf(p.getUpdatedAt()));
        }
    }

    private void exportClients(Workbook wb) {
        Sheet sh = wb.createSheet("CLIENTES");
        Row header = sh.createRow(0);
        String[] cols = {"ID", "Nombre", "Email", "Teléfono", "CUIT", "Creado", "Actualizado"};
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        var list = clientRepo.findAll();
        int r = 1;
        for (Client c : list) {
            Row row = sh.createRow(r++);
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getName());
            row.createCell(2).setCellValue(c.getEmail());
            row.createCell(3).setCellValue(c.getPhone());
            row.createCell(4).setCellValue(c.getTaxId());
            row.createCell(5).setCellValue(String.valueOf(c.getCreatedAt()));
            row.createCell(6).setCellValue(String.valueOf(c.getUpdatedAt()));
        }
    }

    private void exportOrders(Workbook wb) {
        Sheet sh = wb.createSheet("ORDENES");
        Row header = sh.createRow(0);
        String[] cols = {"ID", "ClienteID", "Estado", "Total", "Creado", "Actualizado"};
        for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

        var list = orderRepo.findAll();
        int r = 1;
        for (Order o : list) {
            Row row = sh.createRow(r++);
            row.createCell(0).setCellValue(o.getId());
            row.createCell(1).setCellValue(o.getClientId() != null ? o.getClientId() : 0);
            row.createCell(2).setCellValue(o.getStatus() != null ? o.getStatus().toString() : "");
            row.createCell(3).setCellValue(o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0);
            row.createCell(4).setCellValue(String.valueOf(o.getCreatedAt()));
           //row.createCell(5).setCellValue(String.valueOf(o.getUpdatedAt()));
        }

    }

    // ========================================================
    // ============ IMPORTACIONES =============================
    // ========================================================

    private void importProducts(Workbook wb, ImportResult acc, boolean dryRun) {
        Sheet sh = wb.getSheet("PRODUCTOS");
        if (sh == null) return;

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row row = sh.getRow(i);
            if (row == null) continue;

            String name = getString(row, 1);
            BigDecimal price = getDecimal(row, 3);
            boolean active = "Sí".equalsIgnoreCase(getString(row, 4));

            if (!dryRun) {
                Product p = new Product();
                p.updateBasicInfo(name, "", price, active);
                productRepo.save(p);
            }
            acc.addUpsert("Producto", name);
        }
    }

    private void importClients(Workbook wb, ImportResult acc, boolean dryRun) {
        Sheet sh = wb.getSheet("CLIENTES");
        if (sh == null) return;

        for (int i = 1; i <= sh.getLastRowNum(); i++) {
            Row row = sh.getRow(i);
            if (row == null) continue;

            String name = getString(row, 1);
            String email = getString(row, 2);
            String phone = getString(row, 3);
            String taxId = getString(row, 4);

            if (!dryRun) {
                Client c = new Client();
                c.updateBasicInfo(name, email, phone, taxId);
                clientRepo.save(c);
            }
            acc.addUpsert("Cliente", name);
        }
    }

    // ========================================================
    // ============ HELPERS ===================================
    // ========================================================

    private String getString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        return cell == null ? "" : cell.getStringCellValue().trim();
    }

    private BigDecimal getDecimal(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        try {
            return new BigDecimal(cell.getStringCellValue().trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
