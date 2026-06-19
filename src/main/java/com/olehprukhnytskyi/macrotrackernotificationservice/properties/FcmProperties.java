package com.olehprukhnytskyi.macrotrackernotificationservice.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.fcm")
public class FcmProperties {
    private boolean enabled;
    private String serviceAccountPath;
    private String serviceAccountJson;
    private String topicPrefix = "macro-tracker-cache-user-";
}
