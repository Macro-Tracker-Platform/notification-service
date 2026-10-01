package com.olehprukhnytskyi.macrotrackernotificationservice.repository;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.NotificationPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findAllByEnabledTrue();
}
