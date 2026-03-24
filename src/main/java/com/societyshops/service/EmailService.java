package com.societyshops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Async
    public void sendShopApprovalRequest(String shopName, String ownerName, String ownerEmail, Long shopId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(adminEmail);
            message.setSubject("New Shop Registration: " + shopName);
            message.setText(
                "A new shop registration is pending your approval.\n\n" +
                "Shop Name : " + shopName + "\n" +
                "Owner     : " + ownerName + "\n" +
                "Email     : " + ownerEmail + "\n\n" +
                "Login to approve or reject:\n" +
                "http://localhost:8080/web/login"
            );
            mailSender.send(message);
            log.info("Approval email sent to admin for shop: {}", shopName);
        } catch (Exception e) {
            log.error("Failed to send approval email: {}", e.getMessage());
        }
    }
}
