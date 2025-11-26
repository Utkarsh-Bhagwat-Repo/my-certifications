package com.healthtrack360.messaging;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.notifications}")
    private String notificationTopic;

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallback")
    @Retry(name = "notificationService")
    public void sendNotification(String message) {
        log.info("Sending notification event: {}", message);
        kafkaTemplate.send(notificationTopic, message);
    }

    public void fallback(String message, Throwable throwable) {
        log.error("Failed to send notification. Message: {}. Reason: {}", message, throwable.getMessage());
    }
}
