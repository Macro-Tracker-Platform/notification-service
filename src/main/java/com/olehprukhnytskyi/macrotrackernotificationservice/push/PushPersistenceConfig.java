package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {
        "com.olehprukhnytskyi.model",
        "com.olehprukhnytskyi.macrotrackernotificationservice.model"
})
public class PushPersistenceConfig {
}
