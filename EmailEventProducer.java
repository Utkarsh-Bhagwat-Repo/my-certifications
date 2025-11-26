package com.healthtrack360.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailEventProducer {

    private static final Logger log = LoggerFactory.getLogger(EmailEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String emailTopic;

    public EmailEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                              @Value("${kafka.topic.emails}") String emailTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailTopic = emailTopic;
        this.objectMapper = new ObjectMapper();
    }

    public void sendEmailEvent(EmailMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            log.info("Producing email event to topic {}: {}", emailTopic, payload);
            kafkaTemplate.send(emailTopic, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize email message", e);
            throw new IllegalStateException("Could not serialize email message");
        }
    }
}
