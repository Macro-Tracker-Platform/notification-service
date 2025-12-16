package com.olehprukhnytskyi.macrotrackernotificationservice.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "notification-service")
public class NotificationServiceProperties {
    @NotBlank
    private String url;
}
