package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreference;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreferencesDto;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.RegisterPushDeviceRequest;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.NotificationPreferenceRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushNotificationService {
    private static final Set<String> SUPPORTED_LOCALES = Set.of(
            "ar", "de", "el", "en", "es", "fr", "hi",
            "id", "pl", "pt", "ru", "tr", "uk");

    private final NotificationPreferenceRepository preferenceRepository;
    private final PushDeviceRepository deviceRepository;

    @Transactional
    public NotificationPreferencesDto getPreferences(Long userId) {
        return toDto(findOrCreate(userId));
    }

    @Transactional
    public NotificationPreferencesDto updatePreferences(
            Long userId, NotificationPreferencesDto request) {
        NotificationPreference preference = findOrCreate(userId);
        preference.setEnabled(request.isEnabled());
        preference.setDinnerEnabled(request.isDinnerEnabled());
        preference.setDinnerTime(request.getDinnerTime().withSecond(0).withNano(0));
        preference.setAiLimitEnabled(request.isAiLimitEnabled());
        preference.setAiLimitTime(request.getAiLimitTime().withSecond(0).withNano(0));
        preference.setWeeklyReportEnabled(request.isWeeklyReportEnabled());
        preference.setWeeklyReportTime(request.getWeeklyReportTime().withSecond(0).withNano(0));
        preference.setUpdatedAt(Instant.now());
        return toDto(preferenceRepository.save(preference));
    }

    @Transactional
    public void registerDevice(Long userId, String deviceId,
                               RegisterPushDeviceRequest request) {
        String token = request.getToken().trim();
        String platform = request.getPlatform().trim().toUpperCase(Locale.ROOT);
        final String zone = normalizeZone(request.getTimeZone());
        if (deviceId == null || deviceId.isBlank() || deviceId.length() > 128
                || !Set.of("ANDROID", "IOS").contains(platform)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid push device");
        }
        PushDevice device = deviceRepository.findByDeviceId(deviceId)
                .orElseGet(PushDevice::new);
        deviceRepository.findByFcmToken(token)
                .filter(existing -> !existing.getId().equals(device.getId()))
                .ifPresent(existing -> {
                    deviceRepository.delete(existing);
                    deviceRepository.flush();
                });
        Instant now = Instant.now();
        if (device.getId() == null) {
            device.setDeviceId(deviceId);
            device.setCreatedAt(now);
        }
        device.setUserId(userId);
        device.setFcmToken(token);
        device.setPlatform(platform);
        device.setActive(true);
        device.setUpdatedAt(now);
        deviceRepository.save(device);

        NotificationPreference preference = findOrCreate(userId);
        preference.setLocale(normalizeLocale(request.getLocale()));
        preference.setTimeZone(zone);
        preference.setUpdatedAt(now);
        preferenceRepository.save(preference);
    }

    @Transactional
    public void unregisterDevice(Long userId, String deviceId) {
        deviceRepository.findByDeviceId(deviceId)
                .filter(device -> userId.equals(device.getUserId()))
                .ifPresent(device -> {
                    device.setActive(false);
                    device.setUpdatedAt(Instant.now());
                    deviceRepository.save(device);
                });
    }

    private NotificationPreference findOrCreate(Long userId) {
        return preferenceRepository.findById(userId).orElseGet(() -> {
            NotificationPreference value = new NotificationPreference();
            Instant now = Instant.now();
            value.setUserId(userId);
            value.setCreatedAt(now);
            value.setUpdatedAt(now);
            return preferenceRepository.save(value);
        });
    }

    private String normalizeLocale(String value) {
        String locale = value.trim().toLowerCase(Locale.ROOT);
        return SUPPORTED_LOCALES.contains(locale) ? locale : "en";
    }

    private String normalizeZone(String value) {
        try {
            return ZoneId.of(value.trim()).getId();
        } catch (java.time.DateTimeException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid time zone");
        }
    }

    private NotificationPreferencesDto toDto(NotificationPreference value) {
        return NotificationPreferencesDto.builder()
                .enabled(value.isEnabled())
                .dinnerEnabled(value.isDinnerEnabled())
                .dinnerTime(value.getDinnerTime())
                .aiLimitEnabled(value.isAiLimitEnabled())
                .aiLimitTime(value.getAiLimitTime())
                .weeklyReportEnabled(value.isWeeklyReportEnabled())
                .weeklyReportTime(value.getWeeklyReportTime())
                .locale(value.getLocale())
                .timeZone(value.getTimeZone())
                .build();
    }
}
