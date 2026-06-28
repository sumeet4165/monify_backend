package com.example.MONEYMANAGER.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String content) {
        int maxAttempts = 3;
        long backoffMs = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (content != null && (content.trim().startsWith("<!DOCTYPE html") || content.trim().startsWith("<html"))) {
                    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setFrom(fromEmail);
                    helper.setTo(to);
                    helper.setSubject(subject);
                    helper.setText(content, true);
                    javaMailSender.send(mimeMessage);
                } else {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(content);
                    javaMailSender.send(message);
                }
                return; // Success
            } catch (Exception e) {
                lastException = e;
                System.err.println("SMTP send attempt " + attempt + " failed for " + to + ": " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(backoffMs * attempt); // exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        }
        throw new RuntimeException("Failed to send email after " + maxAttempts + " attempts. Last error: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }

    public void sendemailwithattachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
        int maxAttempts = 3;
        long backoffMs = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(body, true);
                helper.addAttachment(filename, new ByteArrayResource(attachment));
                javaMailSender.send(mimeMessage);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                System.err.println("SMTP send with attachment attempt " + attempt + " failed for " + to + ": " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(backoffMs * attempt); // exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                }
            }
        }
        throw new MessagingException("Failed to send email with attachment after " + maxAttempts + " attempts. Last error: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }
}
