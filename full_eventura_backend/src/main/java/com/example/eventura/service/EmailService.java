package com.example.eventura.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String mailSenderUsername;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.support-email}")
    private String supportEmail;

    public void sendWelcomeEmail(String to, String subject, String firstName, String lastName) throws MessagingException {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);

        String body = templateEngine.process("welcome-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content

        // Attach images as inline resources
        try {
            ClassPathResource logoImage = new ClassPathResource("static/images/logo2.png");
            helper.addInline("logo2", logoImage);
            logger.debug("Attached inline image: logo2.png");
        } catch (Exception e) {
            logger.error("Failed to attach images: {}", e.getMessage());
            throw new MessagingException("Failed to attach images", e);
        }

        mailSender.send(message);
        logger.debug("Sent email to {} with subject: {}", to, subject);
    }

    public void sendConnectionRejectionEmail(String to, String subject, String clientFirstName, String clientLastName,
                                             String eventDetails, String providerFirstName, String providerLastName)
            throws MessagingException {
        Context context = new Context();
        context.setVariable("clientFirstName", clientFirstName);
        context.setVariable("clientLastName", clientLastName);
        context.setVariable("eventDetails", eventDetails);
        context.setVariable("providerFirstName", providerFirstName);
        context.setVariable("providerLastName", providerLastName);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);

        String body = templateEngine.process("connection-rejection", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content

        // Attach images as inline resources
        try {
            ClassPathResource logoImage = new ClassPathResource("static/images/logo2.png");
            helper.addInline("logo2", logoImage);
            logger.debug("Attached inline image: logo2.png");
        } catch (Exception e) {
            logger.error("Failed to attach images: {}", e.getMessage());
            throw new MessagingException("Failed to attach images", e);
        }

        mailSender.send(message);
        logger.debug("Sent rejection email to {} with subject: {}", to, subject);
    }

    public void sendConnectionAcceptanceEmail(String to, String subject, String clientFirstName, String clientLastName,
                                              String eventDetails, String providerFirstName, String providerLastName)
            throws MessagingException {
        Context context = new Context();
        context.setVariable("clientFirstName", clientFirstName);
        context.setVariable("clientLastName", clientLastName);
        context.setVariable("eventDetails", eventDetails);
        context.setVariable("providerFirstName", providerFirstName);
        context.setVariable("providerLastName", providerLastName);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);

        String body = templateEngine.process("connection-acceptance", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content

        // Attach images as inline resources
        try {
            ClassPathResource logoImage = new ClassPathResource("static/images/logo2.png");
            helper.addInline("logo2", logoImage);
            logger.debug("Attached inline image: logo2.png");
        } catch (Exception e) {
            logger.error("Failed to attach images: {}", e.getMessage());
            throw new MessagingException("Failed to attach images", e);
        }

        mailSender.send(message);
        logger.debug("Sent acceptance email to {} with subject: {}", to, subject);
    }

    public void sendConnectionRequestEmail(String to, String subject, String providerFirstName, String providerLastName,
                                           String clientFirstName, String clientLastName, String eventDetails,
                                           String proposedDate) throws MessagingException {
        Context context = new Context();
        context.setVariable("providerFirstName", providerFirstName);
        context.setVariable("providerLastName", providerLastName);
        context.setVariable("clientFirstName", clientFirstName);
        context.setVariable("clientLastName", clientLastName);
        context.setVariable("eventDetails", eventDetails);
        context.setVariable("proposedDate", proposedDate);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);

        String body = templateEngine.process("connection-request", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content

        // Attach images as inline resources
        try {
            ClassPathResource logoImage = new ClassPathResource("static/images/logo2.png");
            helper.addInline("logo2", logoImage);
            logger.debug("Attached inline image: logo2.png");
        } catch (Exception e) {
            logger.error("Failed to attach images: {}", e.getMessage());
            throw new MessagingException("Failed to attach images", e);
        }

        mailSender.send(message);
        logger.debug("Sent connection request email to {} with subject: {}", to, subject);
    }

    public void sendProfileUpdateEmail(String to, String subject, String firstName, String lastName) throws MessagingException {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("appUrl", appUrl);
        context.setVariable("supportEmail", supportEmail);

        String body = templateEngine.process("profile-update", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailSenderUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true for HTML content

        // Attach images as inline resources
        try {
            ClassPathResource logoImage = new ClassPathResource("static/images/logo2.png");
            helper.addInline("logo2", logoImage);
            logger.debug("Attached inline image: logo2.png");
        } catch (Exception e) {
            logger.error("Failed to attach images: {}", e.getMessage());
            throw new MessagingException("Failed to attach images", e);
        }

        mailSender.send(message);
        logger.debug("Sent profile update email to {} with subject: {}", to, subject);
    }

}
