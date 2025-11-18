package com.products.orders.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @JsonManagedReference
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (status == null) status = OrderStatus.PENDING;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;

        recalculateTotals();
    }

    @PreUpdate
    void preUpdate() {
        recalculateTotals();
    }

    public void approve() {
        this.status = OrderStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    public void recalculateTotals() {
        BigDecimal total = BigDecimal.ZERO;

        if (items != null) {
            for (OrderItem item : items) {
                if (item == null) continue;

                // nos aseguramos de que el item tenga su lineTotal correcto
                item.recalcLineTotal();

                if (item.getLineTotal() != null) {
                    total = total.add(item.getLineTotal());
                }
            }
        }

        this.totalAmount = total;
    }

    public Long getId() { return id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item == null) return;
        items.add(item);
        item.setOrder(this);
    }

    public void addItems(List<OrderItem> newItems) {
        if (newItems == null) return;
        for (OrderItem it : newItems) addItem(it);
    }

    public void removeItem(OrderItem item) {
        if (item == null) return;
        items.remove(item);
        item.setOrder(null);
    }
}
