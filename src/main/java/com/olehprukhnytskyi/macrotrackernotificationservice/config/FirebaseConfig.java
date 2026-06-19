package com.olehprukhnytskyi.macrotrackernotificationservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.olehprukhnytskyi.macrotrackernotificationservice.properties.FcmProperties;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {
    private final FcmProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "app.fcm", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(googleCredentials())
                .build();
        FirebaseApp app = FirebaseApp.getApps().stream()
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(options));
        return FirebaseMessaging.getInstance(app);
    }

    private GoogleCredentials googleCredentials() throws IOException {
        try (InputStream inputStream = credentialsStream()) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }

    private InputStream credentialsStream() throws IOException {
        if (StringUtils.hasText(properties.getServiceAccountJson())) {
            return new ByteArrayInputStream(properties.getServiceAccountJson()
                    .getBytes(StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(properties.getServiceAccountPath())) {
            return new FileInputStream(properties.getServiceAccountPath());
        }
        throw new IllegalStateException("FCM is enabled but service account is not configured");
    }
}
