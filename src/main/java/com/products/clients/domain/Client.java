// src/main/java/com/products/clients/domain/Client.java
package com.products.clients.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
    name = "clients",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_clients_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_clients_tax_id", columnNames = "tax_id")
    },
    indexes = {
        @Index(name = "idx_clients_name", columnList = "name"),
        @Index(name = "idx_clients_email", columnList = "email")
    }
)
public class Client {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String name;

    @Email
    @Column(length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(name = "tax_id", length = 40)
    private String taxId;

    // LAZY para evitar N+1 al paginar
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    // ===== Helpers de colección =====
    /** Reemplaza toda la lista conservando la relación bidireccional */
    public void setAddresses(List<Address> addresses) {
        this.addresses.clear();
        if (addresses != null) {
            for (Address a : addresses) addAddress(a);
        }
    }

    public void replaceAddresses(List<Address> newAddresses) {
        this.addresses.clear();
        if (newAddresses != null) {
            for (Address a : newAddresses) addAddress(a);
        }
    }

    public void addAddress(Address address) {
        if (address == null) return;
        address.setClient(this);
        this.addresses.add(address);
    }

    public void removeAddress(Address a) {
        if (a == null) return;
        a.setClient(null);
        this.addresses.remove(a);
    }

    public void removeAddress(Long addressId) {
        this.addresses.removeIf(a -> Objects.equals(a.getId(), addressId));
    }

    // ===== Actualización básica en bloque (opcional) =====
    public void updateBasicInfo(String name, String email, String phone, String taxId) {
        if (name  != null && !name.isBlank())  this.name  = name.trim();
        if (email != null)                     this.email = email.trim();
        if (phone != null)                     this.phone = phone.trim();
        if (taxId != null)                     this.taxId = taxId.trim();
    }

    // ===== Getters / Setters mínimos requeridos por el import/export =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null) ? null : name.trim(); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = (email == null) ? null : email.trim(); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = (phone == null) ? null : phone.trim(); }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = (taxId == null) ? null : taxId.trim(); }

    public List<Address> getAddresses() { return addresses; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; } // opcional

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; } // opcional
}
