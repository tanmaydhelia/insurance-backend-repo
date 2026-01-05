package com.insurance_notification.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.insurance_notification.client.AuthClient;
import com.insurance_notification.dto.NotificationEvent;
import com.insurance_notification.dto.UserDTO;
import com.insurance_notification.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

	@Autowired
	private final EmailService emailService;
    @Autowired
	private final AuthClient authClient; 

    @KafkaListener(topics = "notification_topic", groupId = "notification-group")
    public void consume(NotificationEvent event) {
        log.info("Received event for User ID: {}", event.getId());

        try {
            // 1. Fetch Email from Identity Service
            UserDTO user = authClient.getUserById(event.getId());
            String email = user.getEmail();

            // 2. Send Email
            if(email != null) {
                emailService.sendEmail(email, event.getSubject(), event.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to fetch user or send email", e);
        }
    }
}