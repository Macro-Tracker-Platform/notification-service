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

    public void sendConfirmationEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            String link = "macrotracker://confirm?token=" + token;
            String html = """
                    <p>Click the link to confirm your email:</p>
                    <p>
                        <a href="%s">Confirm email</a>
                    </p>
                    """.formatted(link);
            helper.setTo(email);
            helper.setSubject("Confirm Your Registration");
            helper.setText(html, true);
            helper.setFrom(emailProperties.getUsername());
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }
}
