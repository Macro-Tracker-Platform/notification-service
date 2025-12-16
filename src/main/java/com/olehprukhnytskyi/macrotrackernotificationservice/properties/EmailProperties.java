package com.olehprukhnytskyi.macrotrackernotificationservice.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "spring.mail")
public class EmailProperties {
    @NotBlank
    private String username;
}
