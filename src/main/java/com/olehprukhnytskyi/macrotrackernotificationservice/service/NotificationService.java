package com.olehprukhnytskyi.macrotrackernotificationservice.service;

import com.olehprukhnytskyi.macrotrackernotificationservice.properties.EmailProperties;
import com.olehprukhnytskyi.macrotrackernotificationservice.properties.NotificationServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationServiceProperties notificationServiceProperties;
    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public void sendConfirmationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirm Your Registration");
        message.setText("Click the link to confirm: "
                + notificationServiceProperties.getUrl()
                + "/api/auth/confirm?token="
                + token);
        message.setFrom(emailProperties.getUsername());
        mailSender.send(message);
    }
}
