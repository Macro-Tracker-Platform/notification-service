package com.olehprukhnytskyi.macrotrackernotificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackernotificationservice.dto.CacheInvalidationEvent;
import com.olehprukhnytskyi.macrotrackernotificationservice.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInvalidationConsumer {
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "cache-invalidation", groupId = "notification-group")
    public void handleCacheInvalidation(String rawJson) throws JsonProcessingException {
        CacheInvalidationEvent event = objectMapper
                .readValue(rawJson, CacheInvalidationEvent.class);
        fcmService.sendCacheInvalidation(event);
    }
}
