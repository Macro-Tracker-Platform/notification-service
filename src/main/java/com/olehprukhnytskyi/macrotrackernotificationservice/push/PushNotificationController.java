package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreferencesDto;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.RegisterPushDeviceRequest;
import com.olehprukhnytskyi.util.CustomHeaders;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class PushNotificationController {
    private static final String X_DEVICE_ID = "X-Device-Id";
    private final PushNotificationService service;

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesDto> getPreferences(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId) {
        return ResponseEntity.ok(service.getPreferences(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferencesDto> updatePreferences(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestBody @Valid NotificationPreferencesDto request) {
        return ResponseEntity.ok(service.updatePreferences(userId, request));
    }

    @PostMapping("/devices")
    public ResponseEntity<Void> registerDevice(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestHeader(X_DEVICE_ID) String deviceId,
            @RequestBody @Valid RegisterPushDeviceRequest request) {
        service.registerDevice(userId, deviceId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/devices/current")
    public ResponseEntity<Void> unregisterDevice(
            @RequestHeader(CustomHeaders.X_USER_ID) Long userId,
            @RequestHeader(X_DEVICE_ID) String deviceId) {
        service.unregisterDevice(userId, deviceId);
        return ResponseEntity.noContent().build();
    }
}
