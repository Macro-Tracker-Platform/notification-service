package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.event.UserDeletedEvent;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PushAccountCleanupConsumer {
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Transactional
    @KafkaListener(topics = "user-deleted", groupId = "notification-group")
    public void handleUserDeleted(String rawJson) throws JsonProcessingException {
        Long userId = objectMapper.readValue(rawJson, UserDeletedEvent.class).getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User deletion requires a user ID");
        }
        entityManager.createQuery("delete from PushDevice where userId = :userId")
                .setParameter("userId", userId).executeUpdate();
        entityManager.createQuery("delete from PushDeliveryLog where userId = :userId")
                .setParameter("userId", userId).executeUpdate();
        entityManager.createQuery("delete from NotificationPreference where userId = :userId")
                .setParameter("userId", userId).executeUpdate();
    }
}
