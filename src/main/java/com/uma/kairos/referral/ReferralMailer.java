package com.umasankar.applytrack.referral;

import com.umasankar.applytrack.auth.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ReferralMailer {
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String host;
    private final String from;

    public ReferralMailer(ObjectProvider<JavaMailSender> mailSenders,
                          @Value("${spring.mail.host:}") String host,
                          @Value("${app.mail.from:}") String from) {
        this.mailSenders = mailSenders;
        this.host = host;
        this.from = from;
    }

    public void send(String recipient, String subject, String body, User sender) {
        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null || host.isBlank() || from.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is not configured");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setReplyTo(sender.getEmail());
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Email delivery failed");
        }
    }
}
