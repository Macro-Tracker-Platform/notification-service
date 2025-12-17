package com.olehprukhnytskyi.macrotrackernotificationservice.service;

import com.olehprukhnytskyi.macrotrackernotificationservice.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public void sendConfirmationEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            String html = """
                    <p>Your email confirmation code:</p>
                    <h2>%s</h2>
                    <p>This code is valid for <b>10 minutes</b>.</p>
                    <p>If you did not request this, please ignore this email.</p>
                    """.formatted(code);
            helper.setTo(email);
            helper.setSubject("Email Confirmation Code");
            helper.setText(html, true);
            helper.setFrom(emailProperties.getUsername());
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    public void sendPasswordResetEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            String html = """
                    <p>Your password reset code:</p>
                    <h2>%s</h2>
                    <p>This code is valid for <b>10 minutes</b>.</p>
                    <p>If you did not request a password reset, please ignore this email.</p>
                    """.formatted(code);
            helper.setTo(email);
            helper.setSubject("Password Reset Code");
            helper.setText(html, true);
            helper.setFrom(emailProperties.getUsername());
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
