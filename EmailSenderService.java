package com.healthtrack360.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.healthtrack360.exception.ExternalServiceException;


import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final String defaultCc;
    private final String fromAddress;

    public EmailSenderService(JavaMailSender mailSender,
                              @Value("${mail.cc.default}") String defaultCc, 
                              @Value("${mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.defaultCc = defaultCc;
		this.fromAddress = fromAddress;
    }

    public void sendEmail(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                            StandardCharsets.UTF_8.name());

            helper.setTo(message.getTo());
            helper.setFrom(fromAddress);

            if (defaultCc != null && !defaultCc.isBlank()) {
                helper.addCc(new InternetAddress(defaultCc));
            }

            List<String> ccList = message.getCc();
            if (ccList != null) {
                for (String cc : ccList) {
                    if (cc != null && !cc.isBlank()) {
                        helper.addCc(new InternetAddress(cc));
                    }
                }
            }

            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), false);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", message.getTo());
        } catch (MessagingException ex) {
            log.error("Failed to send email to {}", message.getTo(), ex);
            throw new ExternalServiceException("Failed to send email via SMTP server");
        }
    }
}
