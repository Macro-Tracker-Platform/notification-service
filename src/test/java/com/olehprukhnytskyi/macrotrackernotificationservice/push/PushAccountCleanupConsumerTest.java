package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import static org.assertj.core.api.Assertions.assertThat;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDeliveryLog;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.RegisterPushDeviceRequest;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.NotificationPreferenceRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeliveryLogRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PushAccountCleanupConsumerTest {
    @Autowired
    private PushNotificationService service;
    @Autowired
    private PushAccountCleanupConsumer consumer;
    @Autowired
    private NotificationPreferenceRepository preferences;
    @Autowired
    private PushDeviceRepository devices;
    @Autowired
    private PushDeliveryLogRepository deliveries;
    @Autowired
    private EntityManager entityManager;

    @Test
    void accountDeletionRemovesOnlyItsOwnPushDataAndIsIdempotent() throws Exception {
        service.registerDevice(7L, "deleted-device", new RegisterPushDeviceRequest(
                "deleted-token", "ANDROID", "uk", "Europe/Kyiv"));
        service.registerDevice(8L, "other-device", new RegisterPushDeviceRequest(
                "other-token", "IOS", "en", "UTC"));
        PushDeliveryLog log = new PushDeliveryLog();
        log.setUserId(7L);
        log.setNotificationType("DINNER");
        log.setLocalDate(LocalDate.now());
        log.setSentAt(Instant.now());
        deliveries.saveAndFlush(log);

        consumer.handleUserDeleted("{\"userId\":7}");
        entityManager.clear();
        consumer.handleUserDeleted("{\"userId\":7}");

        assertThat(preferences.findById(7L)).isEmpty();
        assertThat(devices.findAllByUserIdAndActiveTrue(7L)).isEmpty();
        assertThat(deliveries.existsByUserIdAndNotificationTypeAndLocalDate(
                7L, "DINNER", LocalDate.now())).isFalse();
        assertThat(preferences.findById(8L)).isPresent();
        assertThat(devices.findAllByUserIdAndActiveTrue(8L)).hasSize(1);
    }
}
