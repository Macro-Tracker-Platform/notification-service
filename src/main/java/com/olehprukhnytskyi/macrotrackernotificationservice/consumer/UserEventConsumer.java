package com.olehprukhnytskyi.macrotrackernotificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.event.RegistrationEvent;
import com.olehprukhnytskyi.macrotrackernotificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper jacksonObjectMapper;

    @KafkaListener(topics = "user-registered", groupId = "notification-group")
    public void handleUserRegistered(String rawJson) throws JsonProcessingException {
        RegistrationEvent event = jacksonObjectMapper
                .readValue(rawJson, RegistrationEvent.class);
        notificationService.sendConfirmationEmail(
                event.getEmail(), event.getConfirmationToken());
    }
}
