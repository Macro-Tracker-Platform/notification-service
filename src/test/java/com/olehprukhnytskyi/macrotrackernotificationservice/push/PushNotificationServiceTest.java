package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreference;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.RegisterPushDeviceRequest;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.NotificationPreferenceRepository;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PushNotificationServiceTest {
    private final NotificationPreferenceRepository preferences =
            mock(NotificationPreferenceRepository.class);
    private final PushDeviceRepository devices = mock(PushDeviceRepository.class);
    private final PushNotificationService service =
            new PushNotificationService(preferences, devices);
    private final NotificationPreference preference = new NotificationPreference();

    @BeforeEach
    void setUp() {
        preference.setUserId(7L);
        preference.setLocale("uk");
        preference.setTimeZone("Europe/Kyiv");
        when(preferences.findById(7L)).thenReturn(Optional.of(preference));
        when(preferences.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void settingsDoNotOverwriteDeviceLocaleOrTimezone() {
        var request = service.getPreferences(7L);
        request.setEnabled(false);
        request.setDinnerTime(LocalTime.of(21, 15, 35));
        request.setLocale("en");
        request.setTimeZone("UTC");
        var saved = service.updatePreferences(7L, request);
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.getDinnerTime()).isEqualTo(LocalTime.of(21, 15));
        assertThat(saved.getLocale()).isEqualTo("uk");
        assertThat(saved.getTimeZone()).isEqualTo("Europe/Kyiv");
    }

    @Test
    void anotherUserCannotUnregisterAnOwnedDevice() {
        PushDevice device = new PushDevice();
        device.setUserId(8L);
        device.setActive(true);
        when(devices.findByDeviceId("device")).thenReturn(Optional.of(device));
        service.unregisterDevice(7L, "device");
        assertThat(device.isActive()).isTrue();
        verify(devices, never()).save(any());
    }

    @Test
    void loginReassignsDeviceAndUpdatesLocaleWithoutEnablingDisabledPreferences() {
        PushDevice device = new PushDevice();
        device.setId(1L);
        device.setUserId(8L);
        preference.setEnabled(false);
        when(devices.findByDeviceId("device")).thenReturn(Optional.of(device));
        service.registerDevice(7L, "device", new RegisterPushDeviceRequest(
                "token", "IOS", "pl", "Europe/Warsaw"));
        assertThat(device.getUserId()).isEqualTo(7L);
        assertThat(device.isActive()).isTrue();
        assertThat(device.getFcmToken()).isEqualTo("token");
        assertThat(preference.getLocale()).isEqualTo("pl");
        assertThat(preference.isEnabled()).isFalse();
    }

    @Test
    void malformedTimezoneDoesNotCreateDevice() {
        assertThatThrownBy(() -> service.registerDevice(7L, "device",
                new RegisterPushDeviceRequest("token", "ANDROID", "en", "unknown-zone")))
                .isInstanceOf(ResponseStatusException.class);
        verify(devices, never()).save(any());
    }

    @Test
    void everyAppLocaleHasLocalizedCopyForEveryReminder() {
        PushMessageCatalog catalog = new PushMessageCatalog();
        for (String locale : new String[]{"ar", "de", "el", "es", "fr", "hi",
                "id", "pl", "pt", "ru", "tr", "uk"}) {
            for (PushMessageCatalog.NotificationType type
                    : PushMessageCatalog.NotificationType.values()) {
                var copy = catalog.message(locale, type);
                assertThat(copy.title()).isNotBlank()
                        .isNotEqualTo(catalog.message("en", type).title());
                assertThat(copy.body()).isNotBlank();
            }
        }
        assertThat(catalog.message("uk", PushMessageCatalog.NotificationType.DINNER).title())
                .isEqualTo("Закрий щоденник за сьогодні 🎯");
    }
}
