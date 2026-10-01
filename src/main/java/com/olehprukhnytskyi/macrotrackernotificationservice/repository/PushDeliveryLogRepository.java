package com.olehprukhnytskyi.macrotrackernotificationservice.repository;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDeliveryLog;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeliveryLogRepository extends JpaRepository<PushDeliveryLog, Long> {
    boolean existsByUserIdAndNotificationTypeAndLocalDate(
            Long userId, String notificationType, LocalDate localDate);
}
