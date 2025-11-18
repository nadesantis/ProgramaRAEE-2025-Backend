// src/main/java/com/products/payments/config/AppUrls.java
package com.products.payments.config;

import org.springframework.stereotype.Component;

@Component
public class AppUrls {

    // ⚠️ Para desarrollo/local. No subir con datos reales a un repo público.
    private static final String FRONTEND_BASE_URL = "http://localhost:4200";
    private static final String MP_NOTIFICATION_URL = "http://localhost:8080/api/mp/webhook";

    public String getFrontendBaseUrl() {
        return FRONTEND_BASE_URL;
    }

    public String getMpNotificationUrl() {
        return MP_NOTIFICATION_URL;
    }
}
