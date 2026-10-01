package com.olehprukhnytskyi.macrotrackernotificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "push_delivery_log")
public class PushDeliveryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "notification_type", nullable = false, length = 32)
    private String notificationType;
    @Column(name = "local_date", nullable = false)
    private LocalDate localDate;
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
