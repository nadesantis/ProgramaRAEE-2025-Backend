package com.products.orders.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(name = "idx_order_items_order", columnList = "order_id")
    }
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonManagedReference  
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotNull
    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @NotNull
    @PositiveOrZero
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @PositiveOrZero
    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    public static OrderItem of(Long productId, String productName, BigDecimal unitPrice, Integer quantity) {
        OrderItem it = new OrderItem();
        it.productId = productId;
        it.productName = productName;
        it.unitPrice = unitPrice;
        it.quantity = quantity;
        it.recalcLineTotal();
        return it;
    }


    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    @Transient
    public Long getOrderId() { return order != null ? order.getId() : null; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; recalcLineTotal();}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; recalcLineTotal();}

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    
    public void recalcLineTotal() {
        if (unitPrice == null || quantity == null) {
            this.lineTotal = BigDecimal.ZERO;
        } else {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity.longValue()));
        }
    }

}
