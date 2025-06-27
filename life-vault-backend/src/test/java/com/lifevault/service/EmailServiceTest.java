package com.lifevault.service;

import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private TrustedContact testContact;

    @BeforeEach
    void setUp() {
        // Set the required properties
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@lifevault.com");
        ReflectionTestUtils.setField(emailService, "appUrl", "http://localhost:3000");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setInactivityPeriodDays(180);

        testContact = new TrustedContact();
        testContact.setId(1L);
        testContact.setName("John Doe");
        testContact.setEmail("john@example.com");
        testContact.setUser(testUser);
        testContact.setVerificationToken(UUID.randomUUID().toString());
    }

    @Test
    void sendContactVerificationEmail_Success() {
        emailService.sendContactVerificationEmail(testContact);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("john@example.com", sentMessage.getTo()[0]);
        assertEquals("Life Vault - Verify Your Email", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("John Doe"));
        assertTrue(sentMessage.getText().contains("Test User"));
        assertTrue(sentMessage.getText().contains("/verify-contact/" + testContact.getVerificationToken()));
    }

    @Test
    void sendInactivityWarningEmail_StandardWarning() {
        emailService.sendInactivityWarningEmail(testUser);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertEquals("Life Vault - Activity Check Required", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Test"));
    }

    @Test
    void sendInactivityWarningEmail_WithTypeAndToken() {
        String token = "test-token-123";
        
        emailService.sendInactivityWarningEmail(testUser, "50% warning", 90, token);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getText().contains("90 days"));
        assertTrue(sentMessage.getText().contains("/api/activity/verify/" + token));
        assertTrue(sentMessage.getText().contains("Routine Check-In"));
    }

    @Test
    void sendInactivityWarningEmail_LastWeekWarning() {
        String token = "test-token-week";
        
        emailService.sendInactivityWarningEmail(testUser, "final week warning", 175, token);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("175 days"));
        assertTrue(sentMessage.getText().contains("URGENT: Final Week Notice"));
    }

    @Test
    void sendInactivityWarningEmail_GracePeriodWarning() {
        String token = "test-token-grace";
        
        emailService.sendInactivityWarningEmail(testUser, "grace period warning", 185, token);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("CRITICAL: Grace Period Active"));
    }

    @Test
    void sendVaultRevealNotification_Success() {
        emailService.sendVaultRevealNotification(testContact, testUser);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("john@example.com", sentMessage.getTo()[0]);
        assertEquals("Life Vault - Important Information", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("John Doe"));
        assertTrue(sentMessage.getText().contains("Test User"));
        assertTrue(sentMessage.getText().contains("/vault-access/" + testContact.getVerificationToken()));
    }

    @Test
    void sendSimpleEmail_ExceptionHandling() {
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Should throw exception when mail server fails
        assertThrows(RuntimeException.class, () -> {
            emailService.sendContactVerificationEmail(testContact);
        });

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendMultipleEmails_Success() {
        emailService.sendContactVerificationEmail(testContact);
        emailService.sendInactivityWarningEmail(testUser);
        emailService.sendVaultRevealNotification(testContact, testUser);

        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailContent_ContainsCorrectUrls() {
        String token = "activity-token";
        
        emailService.sendInactivityWarningEmail(testUser, "test", 100, token);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String content = sentMessage.getText();
        
        // Verify it contains both the one-click link and the regular login link
        assertTrue(content.contains("http://localhost:3000/api/activity/verify/" + token));
        assertTrue(content.contains("Alternative: You can also log in at: http://localhost:3000"));
    }
}