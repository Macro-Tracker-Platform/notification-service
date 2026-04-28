package com.olehprukhnytskyi.macrotrackernotificationservice.service;

import com.olehprukhnytskyi.macrotrackernotificationservice.properties.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public void sendConfirmationEmail(String email, String code) {
        String html = buildHtml("Your email confirmation code:", code, "this");
        sendEmail(email, "Email Confirmation Code", html);
    }

    public void sendPasswordResetEmail(String email, String code) {
        String html = buildHtml("Your password reset code:", code, "a password reset");
        sendEmail(email, "Password Reset Code", html);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress(emailProperties.getUsername(), "Macro Tracker"));

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email: " + subject, e);
        } catch (MailException e) {
            log.error("Failed to send email to {}. Reason: {}", to, e.getMessage());
        }
    }

    private String buildHtml(String title, String code, String actionName) {
        return """
                <p>%s</p>
                <h2>%s</h2>
                <p>This code is valid for <b>10 minutes</b>.</p>
                <p>If you did not request %s, please ignore this email.</p>
                """.formatted(title, code, actionName);
    }
}
