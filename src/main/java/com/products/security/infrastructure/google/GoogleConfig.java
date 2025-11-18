package com.products.security.infrastructure.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleConfig {

    // 👉 ACA pones tu client-id, sin usar yml
    private static final String GOOGLE_CLIENT_ID =
            "524581011070-bvs8bepgpac2ijms5acr2e1po776m1h9.apps.googleusercontent.com";

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
    }
}
