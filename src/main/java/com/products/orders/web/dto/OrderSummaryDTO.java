package com.products.orders.web.dto;

import com.products.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderSummaryDTO(
    Long id,
    Instant createdAt,
    OrderStatus status,
    BigDecimal totalAmount,
    String clientName,
    String clientTaxId,
    List<String> productNames
) {
    // Constructor explícito (aunque Java ya lo genera)
    public OrderSummaryDTO(
        Long id,
        Instant createdAt,
        OrderStatus status,
        BigDecimal totalAmount,
        String clientName,
        String clientTaxId,
        List<String> productNames
    ) {
        // Podés agregar validaciones o defaults si querés
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.status = status;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.clientName = clientName != null ? clientName : "—";
        this.clientTaxId = clientTaxId != null ? clientTaxId : "—";
        this.productNames = productNames != null ? List.copyOf(productNames) : List.of();
    }
}
