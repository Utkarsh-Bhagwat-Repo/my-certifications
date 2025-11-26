package com.healthtrack360.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);

    private final EmailSenderService emailSenderService;
    private final ObjectMapper objectMapper;

    public EmailEventListener(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "${kafka.topic.emails}", groupId = "healthtrack-email-group")
    public void onEmailEvent(String messageJson) {
        try {
            EmailMessage message = objectMapper.readValue(messageJson, EmailMessage.class);
            log.info("Consuming email event for {}", message.getTo());
            emailSenderService.sendEmail(message);
        } catch (Exception e) {
            log.error("Failed to process email event: {}", messageJson, e);
        }
    }
}
