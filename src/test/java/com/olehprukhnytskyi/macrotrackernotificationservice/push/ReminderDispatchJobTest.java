package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreference;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDeliveryLog;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import com.olehprukhnytskyi.macrotrackernotificationservice.push.PushMessageCatalog.NotificationType;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.NotificationPreferenceRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeliveryLogRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReminderDispatchJobTest {
    private final NotificationPreferenceRepository preferences =
            mock(NotificationPreferenceRepository.class);
    private final PushDeviceRepository devices = mock(PushDeviceRepository.class);
    private final PushDeliveryLogRepository deliveries = mock(PushDeliveryLogRepository.class);
    private final ReminderEligibilityClient eligibility = mock(ReminderEligibilityClient.class);
    private final PushFcmSender sender = mock(PushFcmSender.class);
    private final ReminderDispatchJob job = new ReminderDispatchJob(preferences, devices,
            deliveries, eligibility, new PushMessageCatalog(), sender);
    private final NotificationPreference preference = new NotificationPreference();
    private final LocalDate today = LocalDate.of(2026, 9, 28);

    @BeforeEach
    void setUp() {
        preference.setUserId(7L);
        preference.setTimeZone("Europe/Kyiv");
        when(devices.findAllByUserIdAndActiveTrue(7L)).thenReturn(List.of(new PushDevice()));
        when(preferences.findById(7L)).thenReturn(Optional.of(preference));
        when(sender.send(anyList(), any(), any())).thenReturn(true);
        ReflectionTestUtils.setField(job, "dinnerMaxMeals", 2);
    }

    @Test
    void dinnerUsesLocalTimeAndRecoversMissedMinute() {
        summary(1200, 2, 4);
        when(eligibility.calorieGoal(7L, today)).thenReturn(2000);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T17:32:00Z"));
        verify(sender).send(anyList(), any(), eq(NotificationType.DINNER));
        verify(deliveries).save(any(PushDeliveryLog.class));
    }

    @Test
    void completedPlanNeverSendsDinner() {
        summary(2000, 2, 4);
        when(eligibility.calorieGoal(7L, today)).thenReturn(2000);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T17:30:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void threeMealsNeverSendsDinner() {
        summary(1200, 3, 4);
        when(eligibility.calorieGoal(7L, today)).thenReturn(2000);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T17:30:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void aiResetRequiresYesterdayExhaustionAndEmptyToday() {
        when(eligibility.wasAiLimitExhausted(7L, today.minusDays(1), "Europe/Kyiv"))
                .thenReturn(true);
        summary(0, 0, 0);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T05:30:00Z"));
        verify(sender).send(anyList(), any(), eq(NotificationType.AI_LIMIT_RESET));
    }

    @Test
    void foodTodaySuppressesAiReset() {
        when(eligibility.wasAiLimitExhausted(7L, today.minusDays(1), "Europe/Kyiv"))
                .thenReturn(true);
        summary(200, 1, 1);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T05:30:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void aiDoesNotSendWithoutExhaustion() {
        job.dispatchForUser(preference, Instant.parse("2026-09-28T05:30:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void weeklyUsesPreviousWeekOnMonday() {
        when(eligibility.weeklyReportReady(7L, today.minusWeeks(1))).thenReturn(true);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(sender).send(anyList(), any(), eq(NotificationType.WEEKLY_REPORT));
    }

    @Test
    void weeklyWithoutReportIsSuppressed() {
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void disablingDuringEligibilityCheckSuppressesDelivery() {
        NotificationPreference disabled = new NotificationPreference();
        disabled.setEnabled(false);
        when(preferences.findById(7L)).thenReturn(Optional.of(disabled));
        when(eligibility.weeklyReportReady(7L, today.minusWeeks(1))).thenReturn(true);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void deliveredNotificationIsNotSentTwice() {
        when(eligibility.weeklyReportReady(7L, today.minusWeeks(1))).thenReturn(true);
        when(deliveries.existsByUserIdAndNotificationTypeAndLocalDate(
                7L, "WEEKLY_REPORT", today)).thenReturn(true);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(sender, never()).send(anyList(), any(), any());
    }

    @Test
    void disabledMasterDoesNotEvaluateEligibility() {
        preference.setEnabled(false);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(eligibility, never()).weeklyReportReady(any(), any());
    }

    @Test
    void failedDeliveryIsNotMarkedAsSent() {
        when(eligibility.weeklyReportReady(7L, today.minusWeeks(1))).thenReturn(true);
        when(sender.send(anyList(), any(), any())).thenReturn(false);
        job.dispatchForUser(preference, Instant.parse("2026-09-28T06:00:00Z"));
        verify(deliveries, never()).save(any());
    }

    private void summary(int calories, int meals, int intakes) {
        when(eligibility.dailySummary(7L, today)).thenReturn(
                new ReminderEligibilityClient.DailySummary(today,
                        BigDecimal.valueOf(calories), meals, intakes));
    }
}
