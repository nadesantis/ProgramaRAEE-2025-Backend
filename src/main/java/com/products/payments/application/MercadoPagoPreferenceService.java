// src/main/java/com/products/payments/application/MercadoPagoPreferenceService.java
package com.products.payments.application;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.products.orders.domain.Order;
import com.products.orders.domain.OrderItem;
import com.products.payments.config.AppUrls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@Service
public class MercadoPagoPreferenceService {

	@Autowired
    private  AppUrls appUrls;

    public MercadoPagoPreferenceService(AppUrls appUrls) {
        this.appUrls = appUrls;
    }

public Preference createPreferenceForOrder(Order order) throws MPException, MPApiException {

    List<PreferenceItemRequest> items = order.getItems().stream()
            .filter(Objects::nonNull)
            .map(this::toPreferenceItem)
            .toList();

    if (items.isEmpty()) {
        throw new IllegalArgumentException("La orden no tiene ítems válidos para MP");
    }

    PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success(appUrls.getFrontendBaseUrl() + "/orders/" + order.getId() + "/success")
            .failure(appUrls.getFrontendBaseUrl() + "/orders/" + order.getId() + "/failure")
            .pending(appUrls.getFrontendBaseUrl() + "/orders/" + order.getId() + "/pending")
            .build();

    PreferenceRequest preferenceRequest = PreferenceRequest.builder()
            .items(items)
            .externalReference(order.getId().toString())
            .backUrls(backUrls)
            // 👇 COMENTAR / BORRAR ESTO POR AHORA
            // .autoReturn("approved")
            .notificationUrl(appUrls.getMpNotificationUrl())
            .build();

    PreferenceClient client = new PreferenceClient();
    return client.create(preferenceRequest);
}

    private PreferenceItemRequest toPreferenceItem(OrderItem item) {
        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        return PreferenceItemRequest.builder()
                .id(String.valueOf(item.getProductId()))
                .title(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(price)
                .currencyId("ARS")
                .build();
    }
}
