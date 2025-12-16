package com.olehprukhnytskyi.macrotrackernotificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(exclude = RedisAutoConfiguration.class)
public class MacroTrackerNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerNotificationServiceApplication.class, args);
    }

}
