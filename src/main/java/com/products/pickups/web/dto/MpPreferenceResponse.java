package com.products.pickups.web.dto;





public record MpPreferenceResponse(
        Long orderId,
        String mpPreferenceId,
        String mpInitPoint,
        java.math.BigDecimal totalAmount
) {}
