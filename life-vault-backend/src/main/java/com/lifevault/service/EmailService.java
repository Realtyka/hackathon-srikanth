package com.lifevault.service;

import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${cors.allowed-origins}")
    private String appUrl;
    
    public void sendContactVerificationEmail(TrustedContact contact) {
        String subject = "Life Vault - Verify Your Email";
        String verificationUrl = appUrl + "/verify-contact/" + contact.getVerificationToken();
        String message = String.format(
            "Hello %s,\n\n" +
            "%s %s has added you as a trusted contact in their Life Vault.\n\n" +
            "Please click the link below to verify your email address:\n%s\n\n" +
            "This link will expire in 7 days.\n\n" +
            "Best regards,\nLife Vault Team",
            contact.getName(),
            contact.getUser().getFirstName(),
            contact.getUser().getLastName(),
            verificationUrl
        );
        
        sendSimpleEmail(contact.getEmail(), subject, message);
    }
    
    public void sendInactivityWarningEmail(User user) {
        sendInactivityWarningEmail(user, "standard", 0, null);
    }
    
    public void sendInactivityWarningEmail(User user, String type, long daysInactive, String activityToken) {
        String verificationUrl = appUrl + "/api/activity/verify/" + activityToken;
        
        String subject = "Life Vault - Activity Check Required";
        String urgency = "";
        String timeRemaining = "";
        
        int totalPeriod = user.getInactivityPeriodDays();
        long daysRemaining = totalPeriod - daysInactive;
        
        switch (type) {
            case "50% warning":
                urgency = "Routine Check-In";
                timeRemaining = String.format("You have %d days remaining before the next check.", daysRemaining);
                break;
            case "75% warning":
                urgency = "Important Reminder";
                timeRemaining = String.format("Only %d days remaining before final notifications begin.", daysRemaining);
                break;
            case "final week warning":
                urgency = "URGENT: Final Week Notice";
                timeRemaining = String.format("Only %d days left! Daily reminders will be sent.", daysRemaining);
                break;
            case "grace period warning":
                urgency = "CRITICAL: Grace Period Active";
                long graceDaysLeft = 14 - (daysInactive - totalPeriod);
                timeRemaining = String.format("Your trusted contacts will be notified in %d days if you don't respond!", graceDaysLeft);
                break;
            default:
                urgency = "Activity Check";
                timeRemaining = "Please log in to confirm you're okay.";
        }
        
        String message = String.format(
            "Hello %s,\n\n" +
            "[%s]\n\n" +
            "It's been %d days since your last activity on Life Vault.\n\n" +
            "%s\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "CLICK HERE TO CONFIRM YOU'RE ACTIVE:\n" +
            "%s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "This one-click link will:\n" +
            "✓ Instantly reset your activity timer\n" +
            "✓ No login required\n" +
            "✓ Valid for 7 days\n\n" +
            "Alternative: You can also log in at: %s\n\n" +
            "Remember: Your trusted contacts will only be notified if you don't respond " +
            "for your full inactivity period (%d days) plus a %d-day grace period.\n\n" +
            "Best regards,\nLife Vault Team",
            user.getFirstName(),
            urgency,
            daysInactive,
            timeRemaining,
            verificationUrl,
            appUrl,
            totalPeriod,
            14
        );
        
        sendSimpleEmail(user.getEmail(), subject, message);
    }
    
    public void sendVaultRevealNotification(TrustedContact contact, User user) {
        String subject = "Life Vault - Important Information";
        String vaultUrl = appUrl + "/vault-access/" + contact.getVerificationToken();
        String message = String.format(
            "Hello %s,\n\n" +
            "%s %s has not responded to our activity checks for an extended period.\n\n" +
            "As a trusted contact, you now have access to their asset information.\n\n" +
            "Access the vault here: %s\n\n" +
            "Please handle this information with care.\n\n" +
            "Best regards,\nLife Vault Team",
            contact.getName(),
            user.getFirstName(),
            user.getLastName(),
            vaultUrl
        );
        
        sendSimpleEmail(contact.getEmail(), subject, message);
    }
    
    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        mailSender.send(message);
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
}