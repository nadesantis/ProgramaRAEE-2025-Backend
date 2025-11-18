package com.products.payments.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

    @PostConstruct
    public void init() {
        // usa tu token de MpSecrets
        MercadoPagoConfig.setAccessToken(MpSecrets.ACCESS_TOKEN);

        if (MpSecrets.ACCESS_TOKEN == null || MpSecrets.ACCESS_TOKEN.isBlank()) {
            throw new IllegalStateException("El ACCESS_TOKEN está vacío en MpSecrets");
        }
    }
}
