package com.olehprukhnytskyi.macrotrackernotificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.event.PasswordResetEvent;
import com.olehprukhnytskyi.event.RegistrationEvent;
import com.olehprukhnytskyi.macrotrackernotificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registered", groupId = "notification-group")
    public void handleUserRegistered(String rawJson) throws JsonProcessingException {
        RegistrationEvent event = objectMapper
                .readValue(rawJson, RegistrationEvent.class);
        notificationService.sendConfirmationEmail(
                event.getEmail(), event.getConfirmationCode());
    }

    @KafkaListener(topics = "user-password-reset", groupId = "notification-group")
    public void handlePasswordReset(String rawJson) throws JsonProcessingException {
        PasswordResetEvent event = objectMapper
                .readValue(rawJson, PasswordResetEvent.class);
        notificationService.sendPasswordResetEmail(
                event.getEmail(), event.getResetCode());
    }
}
