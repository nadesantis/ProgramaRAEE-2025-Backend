// src/main/java/com/products/catalog/domain/Product.java
package com.products.catalog.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_name",   columnList = "name"),
        @Index(name = "idx_products_active", columnList = "active")
    },
    uniqueConstraints = @UniqueConstraint(name = "uk_products_name", columnNames = "name")
)
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1024)
    private String description;

    @NotNull @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    // ===== Actualización básica en bloque (opcional) =====
    public void updateBasicInfo(String name, String description, BigDecimal unitPrice, Boolean active) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description.trim();
        if (unitPrice != null && unitPrice.signum() >= 0) this.unitPrice = unitPrice;
        if (active != null) this.active = active;
    }

    public void activate()   { this.active = true; }
    public void deactivate() { this.active = false; }

    // ===== Getters / Setters mínimos requeridos por el import/export =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null) ? null : name.trim(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = (description == null) ? null : description.trim(); }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = (unitPrice == null) ? BigDecimal.ZERO : unitPrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; } // opcional

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; } // opcional
}
