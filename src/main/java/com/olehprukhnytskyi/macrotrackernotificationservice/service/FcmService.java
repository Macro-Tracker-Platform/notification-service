package com.olehprukhnytskyi.macrotrackernotificationservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.olehprukhnytskyi.macrotrackernotificationservice.dto.CacheInvalidationEvent;
import com.olehprukhnytskyi.macrotrackernotificationservice.properties.FcmProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {
    private static final String TYPE_CACHE_INVALIDATED = "CACHE_INVALIDATED";
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final FcmProperties properties;

    public void sendCacheInvalidation(CacheInvalidationEvent event) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (!properties.isEnabled() || firebaseMessaging == null) {
            log.warn("FCM is disabled or unconfigured; "
                            + "skipping cache invalidation for userId={} domain={}",
                    event.getUserId(), event.getDomain());
            return;
        }
        try {
            String messageId = firebaseMessaging.send(Message.builder()
                    .setTopic(topicForUser(event.getUserId()))
                    .putAllData(dataPayload(event))
                    .build());
            log.debug("Sent FCM cache invalidation messageId={} userId={} domain={}",
                    messageId, event.getUserId(), event.getDomain());
        } catch (Exception e) {
            log.error("Failed to send FCM cache invalidation userId={} domain={}",
                    event.getUserId(), event.getDomain(), e);
        }
    }

    private Map<String, String> dataPayload(CacheInvalidationEvent event) {
        Map<String, String> data = new HashMap<>();
        data.put("type", TYPE_CACHE_INVALIDATED);
        data.put("domain", event.getDomain());
        data.put("changedAt", event.getChangedAt().toString());
        data.put("originDeviceId", event.getOriginDeviceId() == null
                ? ""
                : event.getOriginDeviceId());
        return data;
    }

    private String topicForUser(Long userId) {
        return properties.getTopicPrefix() + userId;
    }
}
