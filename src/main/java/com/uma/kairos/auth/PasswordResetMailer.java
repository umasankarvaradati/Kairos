package com.umasankar.applytrack.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetMailer {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetMailer.class);
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String host;
    private final String from;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSenders,
                               @Value("${spring.mail.host:}") String host,
                               @Value("${app.mail.from:}") String from) {
        this.mailSenders = mailSenders;
        this.host = host;
        this.from = from;
    }

    public boolean sendCode(String recipient, String code) {
        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null || host.isBlank() || from.isBlank()) {
            log.warn("Password reset email not sent because SMTP is not configured");
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject("Kairos password reset code");
            helper.setText("Your Kairos password reset code is " + code + ". It expires in 10 minutes. "
                + "If you did not request a password reset, you can ignore this email.", false);
            mailSender.send(message);
            return true;
        } catch (MailException | MessagingException exception) {
            log.warn("Password reset email delivery failed", exception);
            return false;
        }
    }
}
