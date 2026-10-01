package com.olehprukhnytskyi.macrotrackernotificationservice.repository;

import com.olehprukhnytskyi.macrotrackernotificationservice.model.PushDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {
    Optional<PushDevice> findByDeviceId(String deviceId);

    Optional<PushDevice> findByFcmToken(String fcmToken);

    List<PushDevice> findAllByUserIdAndActiveTrue(Long userId);
}
