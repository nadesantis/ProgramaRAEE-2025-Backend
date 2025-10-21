package com.products.catalog.web;

import com.products.catalog.application.*;
import com.products.catalog.domain.Product;
import com.products.catalog.web.dto.ProductRequest;
import com.products.catalog.web.dto.ProductResponse;

import com.products.audit.application.AuditService;
import com.products.audit.domain.AuditAction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final CreateProductUseCase createUC;
  private final UpdateProductUseCase updateUC;
  private final DeleteProductUseCase deleteUC;
  private final GetProductUseCase getUC;
  private final ListProductsUseCase listUC;
  private final AuditService audit;

  public ProductController(CreateProductUseCase createUC,
                           UpdateProductUseCase updateUC,
                           DeleteProductUseCase deleteUC,
                           GetProductUseCase getUC,
                           ListProductsUseCase listUC,
                           AuditService audit) {
    this.createUC = createUC;
    this.updateUC = updateUC;
    this.deleteUC = deleteUC;
    this.getUC = getUC;
    this.listUC = listUC;
    this.audit = audit;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
    Product p = createUC.handle(req.getName(), req.getDescription(), req.getUnitPrice(), req.getActive());
    audit.success(AuditAction.PRODUCT_CREATE, "Product", p.getId(), "Creado: " + p.getName());
    return ResponseEntity.ok(toResponse(p));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
    Product p = updateUC.handle(id, req.getName(), req.getDescription(), req.getUnitPrice(), req.getActive());
    audit.success(AuditAction.PRODUCT_UPDATE, "Product", id, "Actualizado: " + p.getName());
    return ResponseEntity.ok(toResponse(p));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','OPERADOR_VENTAS')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteUC.handle(id);
    audit.success(AuditAction.PRODUCT_DELETE, "Product", id, "Eliminado");
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public ResponseEntity<ProductResponse> get(@PathVariable Long id) {
    return getUC.handle(id)
        .map(p -> {
          audit.success(AuditAction.PRODUCT_READ, "Product", id, "Detalle");
          return ResponseEntity.ok(toResponse(p));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN','ADMIN_VENTAS','OPERADOR_VENTAS')")
  public Page<ProductResponse> list(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) Boolean active,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    var result = listUC.handle(name, active, pageable).map(this::toResponse);
    audit.success(AuditAction.PRODUCT_LIST, "Listado productos");
    return result;
  }

  private ProductResponse toResponse(Product p) {
    ProductResponse r = new ProductResponse();
    r.setId(p.getId());
    r.setName(p.getName());
    r.setDescription(p.getDescription());
    r.setUnitPrice(p.getUnitPrice());
    r.setActive(p.isActive());
    r.setCreatedAt(p.getCreatedAt());
    r.setUpdatedAt(p.getUpdatedAt());
    return r;
  }
}
