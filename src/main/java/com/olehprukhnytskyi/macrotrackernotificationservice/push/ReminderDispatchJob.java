package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreference;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDeliveryLog;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import com.olehprukhnytskyi.macrotrackernotificationservice.push.PushMessageCatalog.NotificationType;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.NotificationPreferenceRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeliveryLogRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderDispatchJob {
    private final NotificationPreferenceRepository preferenceRepository;
    private final PushDeviceRepository deviceRepository;
    private final PushDeliveryLogRepository deliveryRepository;
    private final ReminderEligibilityClient eligibilityClient;
    private final PushMessageCatalog messageCatalog;
    private final PushFcmSender sender;
    @Value("${notifications.dinner-max-meals:2}")
    private int dinnerMaxMeals;

    @Scheduled(fixedDelayString = "${notifications.scheduler-delay-ms:60000}")
    @SchedulerLock(name = "pushReminderDispatch",
            lockAtMostFor = "PT10M", lockAtLeastFor = "PT20S")
    public void dispatchDueNotifications() {
        Instant now = Instant.now();
        for (NotificationPreference preference
                : preferenceRepository.findAllByEnabledTrue()) {
            try {
                dispatchForUser(preference, now);
            } catch (RuntimeException exception) {
                log.error("Push dispatch failed for userId={}",
                        preference.getUserId(), exception);
            }
        }
    }

    void dispatchForUser(NotificationPreference preference, Instant now) {
        if (!preference.isEnabled()) {
            return;
        }
        List<PushDevice> devices = deviceRepository
                .findAllByUserIdAndActiveTrue(preference.getUserId());
        if (devices.isEmpty()) {
            return;
        }
        ZonedDateTime localNow = now.atZone(safeZone(preference.getTimeZone()));
        LocalDate today = localNow.toLocalDate();
        LocalTime currentMinute = localNow.toLocalTime().withSecond(0).withNano(0);

        if (preference.isDinnerEnabled()
                && isDue(currentMinute, preference.getDinnerTime())) {
            dispatchIfEligible(preference, devices, NotificationType.DINNER, today,
                    dinnerEligible(preference.getUserId(), today));
        }
        if (preference.isAiLimitEnabled()
                && isDue(currentMinute, preference.getAiLimitTime())) {
            dispatchIfEligible(preference, devices, NotificationType.AI_LIMIT_RESET,
                    today, aiResetEligible(preference.getUserId(), today,
                            preference.getTimeZone()));
        }
        if (preference.isWeeklyReportEnabled()
                && localNow.getDayOfWeek() == DayOfWeek.MONDAY
                && isDue(currentMinute, preference.getWeeklyReportTime())) {
            LocalDate previousMonday = today.minusWeeks(1);
            dispatchIfEligible(preference, devices, NotificationType.WEEKLY_REPORT,
                    today, eligibilityClient.weeklyReportReady(
                            preference.getUserId(), previousMonday));
        }
    }

    private boolean dinnerEligible(Long userId, LocalDate today) {
        ReminderEligibilityClient.DailySummary summary =
                eligibilityClient.dailySummary(userId, today);
        Integer goal = eligibilityClient.calorieGoal(userId, today);
        return summary != null && goal != null && goal > 0
                && summary.mealCount() > 0
                && summary.mealCount() <= dinnerMaxMeals
                && summary.calories() != null
                && summary.calories().compareTo(BigDecimal.valueOf(goal)) < 0;
    }

    private boolean aiResetEligible(Long userId, LocalDate today, String timeZone) {
        if (!eligibilityClient.wasAiLimitExhausted(userId, today.minusDays(1), timeZone)) {
            return false;
        }
        ReminderEligibilityClient.DailySummary summary =
                eligibilityClient.dailySummary(userId, today);
        return summary != null && summary.intakeCount() == 0;
    }

    private void dispatchIfEligible(NotificationPreference preference,
                                    List<PushDevice> devices,
                                    NotificationType type,
                                    LocalDate localDate,
                                    boolean eligible) {
        if (!eligible || deliveryRepository
                .existsByUserIdAndNotificationTypeAndLocalDate(
                        preference.getUserId(), type.name(), localDate)) {
            return;
        }
        NotificationPreference latest = preferenceRepository.findById(
                preference.getUserId()).orElse(null);
        if (latest == null || !latest.isEnabled()
                || !latest.getTimeZone().equals(preference.getTimeZone())
                || (type == NotificationType.DINNER && !latest.isDinnerEnabled())
                || (type == NotificationType.AI_LIMIT_RESET && !latest.isAiLimitEnabled())
                || (type == NotificationType.WEEKLY_REPORT && !latest.isWeeklyReportEnabled())) {
            return;
        }
        boolean sent = sender.send(devices,
                messageCatalog.message(latest.getLocale(), type), type);
        if (!sent) {
            return;
        }
        PushDeliveryLog delivery = new PushDeliveryLog();
        delivery.setUserId(preference.getUserId());
        delivery.setNotificationType(type.name());
        delivery.setLocalDate(localDate);
        delivery.setSentAt(Instant.now());
        deliveryRepository.save(delivery);
    }

    private ZoneId safeZone(String value) {
        try {
            return ZoneId.of(value);
        } catch (RuntimeException exception) {
            return ZoneId.of("UTC");
        }
    }

    private boolean isDue(LocalTime current, LocalTime scheduled) {
        long minutes = java.time.Duration.between(scheduled.withSecond(0)
                .withNano(0), current).toMinutes();
        return minutes >= 0 && minutes < 15;
    }
}
