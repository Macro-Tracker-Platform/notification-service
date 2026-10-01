package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import com.olehprukhnytskyi.macrotrackernotificationservice.properties.FcmProperties;
import com.olehprukhnytskyi.macrotrackernotificationservice.repository.PushDeviceRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushFcmSender {
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final FcmProperties properties;
    private final PushDeviceRepository deviceRepository;

    public boolean send(List<PushDevice> devices, PushMessageCatalog.PushCopy copy,
                        PushMessageCatalog.NotificationType type) {
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (!properties.isEnabled() || messaging == null || devices.isEmpty()) {
            return false;
        }
        List<String> tokens = devices.stream().map(PushDevice::getFcmToken).toList();
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(copy.title()).setBody(copy.body()).build())
                .putData("type", type.name())
                .putData("route", type.route())
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(type == PushMessageCatalog.NotificationType.DINNER
                                ? 0L : 15 * 60 * 1000L)
                        .setPriority(AndroidConfig.Priority.HIGH).build())
                .setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-expiration",
                                type == PushMessageCatalog.NotificationType.DINNER ? "0"
                                        : String.valueOf(Instant.now().plusSeconds(900)
                                        .getEpochSecond()))
                        .setAps(Aps.builder().setSound("default").build()).build())
                .build();
        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            deactivateInvalidTokens(devices, response.getResponses());
            log.info("Sent push type={} successCount={} failureCount={}",
                    type, response.getSuccessCount(), response.getFailureCount());
            return response.getSuccessCount() > 0;
        } catch (Exception exception) {
            log.error("Failed to send push type={}", type, exception);
            return false;
        }
    }

    private void deactivateInvalidTokens(List<PushDevice> devices,
                                         List<SendResponse> responses) {
        for (int index = 0; index < responses.size(); index++) {
            SendResponse response = responses.get(index);
            if (response.isSuccessful() || response.getException() == null) {
                continue;
            }
            String code = response.getException().getMessagingErrorCode() == null
                    ? "" : response.getException().getMessagingErrorCode().name();
            if (!"UNREGISTERED".equals(code) && !"INVALID_ARGUMENT".equals(code)) {
                continue;
            }
            PushDevice device = devices.get(index);
            device.setActive(false);
            device.setUpdatedAt(Instant.now());
            deviceRepository.save(device);
        }
    }
}
