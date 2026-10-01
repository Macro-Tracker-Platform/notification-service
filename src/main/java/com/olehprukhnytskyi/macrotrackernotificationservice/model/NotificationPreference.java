package com.olehprukhnytskyi.macrotrackernotificationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "notification_preferences")
public class NotificationPreference {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "dinner_enabled", nullable = false)
    private boolean dinnerEnabled = true;
    @Column(name = "dinner_time", nullable = false)
    private LocalTime dinnerTime = LocalTime.of(20, 30);
    @Column(name = "ai_limit_enabled", nullable = false)
    private boolean aiLimitEnabled = true;
    @Column(name = "ai_limit_time", nullable = false)
    private LocalTime aiLimitTime = LocalTime.of(8, 30);
    @Column(name = "weekly_report_enabled", nullable = false)
    private boolean weeklyReportEnabled = true;
    @Column(name = "weekly_report_time", nullable = false)
    private LocalTime weeklyReportTime = LocalTime.of(9, 0);
    @Column(nullable = false, length = 8)
    private String locale = "en";
    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone = "UTC";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
