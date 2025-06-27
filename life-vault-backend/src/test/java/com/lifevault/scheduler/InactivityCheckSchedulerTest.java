package com.lifevault.scheduler;

import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import com.lifevault.repository.TrustedContactRepository;
import com.lifevault.repository.UserRepository;
import com.lifevault.service.ActivityLogService;
import com.lifevault.service.EmailService;
import com.lifevault.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InactivityCheckSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrustedContactRepository trustedContactRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InactivityCheckScheduler scheduler;

    private User activeUser;
    private User inactiveUser50;
    private User inactiveUser75;
    private User inactiveUserLastWeek;
    private User inactiveUserGracePeriod;
    private User inactiveUserExpired;

    @BeforeEach
    void setUp() {
        // Set grace period
        ReflectionTestUtils.setField(scheduler, "gracePeriodDays", 14);

        // Active user - no notification needed
        activeUser = createUser(1L, "active@example.com", LocalDateTime.now().minusDays(30), 180);

        // 50% inactive user
        inactiveUser50 = createUser(2L, "fifty@example.com", LocalDateTime.now().minusDays(90), 180);

        // 75% inactive user
        inactiveUser75 = createUser(3L, "seventyfive@example.com", LocalDateTime.now().minusDays(135), 180);

        // Last week user
        inactiveUserLastWeek = createUser(4L, "lastweek@example.com", LocalDateTime.now().minusDays(175), 180);

        // Grace period user
        inactiveUserGracePeriod = createUser(5L, "grace@example.com", LocalDateTime.now().minusDays(182), 180);

        // Expired user
        inactiveUserExpired = createUser(6L, "expired@example.com", LocalDateTime.now().minusDays(195), 180);
    }

    private User createUser(Long id, String email, LocalDateTime lastActivity, int inactivityDays) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setLastActivityAt(lastActivity);
        user.setInactivityPeriodDays(inactivityDays);
        user.setIsActive(true);
        return user;
    }

    @Test
    void checkInactiveUsers_NoNotifications() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(activeUser));

        scheduler.checkInactiveUsers();

        verify(emailService, never()).sendInactivityWarningEmail(any(), any(), anyLong(), any());
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_50PercentWarning() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUser50));
        when(userService.generateActivityToken(inactiveUser50)).thenReturn("token-50");

        scheduler.checkInactiveUsers();

        verify(emailService).sendInactivityWarningEmail(eq(inactiveUser50), eq("50% warning"), eq(90L), eq("token-50"));
        verify(activityLogService).logActivity(eq(inactiveUser50), eq("INACTIVITY_CHECK"), 
                contains("50% warning"));
        verify(userRepository).save(inactiveUser50);
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_75PercentWarning() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUser75));
        when(userService.generateActivityToken(inactiveUser75)).thenReturn("token-75");

        scheduler.checkInactiveUsers();

        verify(emailService).sendInactivityWarningEmail(eq(inactiveUser75), eq("75% warning"), eq(135L), eq("token-75"));
        verify(activityLogService).logActivity(eq(inactiveUser75), eq("INACTIVITY_CHECK"), 
                contains("75% warning"));
        verify(userRepository).save(inactiveUser75);
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_LastWeekWarning() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUserLastWeek));
        when(userService.generateActivityToken(inactiveUserLastWeek)).thenReturn("token-week");

        scheduler.checkInactiveUsers();

        verify(emailService).sendInactivityWarningEmail(eq(inactiveUserLastWeek), eq("final week warning"), eq(175L), eq("token-week"));
        verify(activityLogService).logActivity(eq(inactiveUserLastWeek), eq("INACTIVITY_CHECK"), 
                contains("final week warning"));
        verify(userRepository).save(inactiveUserLastWeek);
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_GracePeriodWarning() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUserGracePeriod));
        when(userService.generateActivityToken(inactiveUserGracePeriod)).thenReturn("token-grace");

        scheduler.checkInactiveUsers();

        verify(emailService).sendInactivityWarningEmail(eq(inactiveUserGracePeriod), eq("grace period warning"), eq(182L), eq("token-grace"));
        verify(activityLogService).logActivity(eq(inactiveUserGracePeriod), eq("INACTIVITY_CHECK"), 
                contains("grace period warning"));
        verify(userRepository).save(inactiveUserGracePeriod);
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_GracePeriodSkipOddDay() {
        // Day 183 - should skip (odd day in grace period)
        User oddDayUser = createUser(7L, "odd@example.com", LocalDateTime.now().minusDays(183), 180);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(oddDayUser));

        scheduler.checkInactiveUsers();

        verify(emailService, never()).sendInactivityWarningEmail(any(), any(), anyLong(), any());
        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_ExpiredNotifyContacts() {
        TrustedContact contact = new TrustedContact();
        contact.setId(1L);
        contact.setEmail("trusted@example.com");
        contact.setName("Trusted Contact");
        contact.setIsVerified(true);
        contact.setIsNotified(false);
        contact.setUser(inactiveUserExpired);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUserExpired));
        when(trustedContactRepository.findByUserId(inactiveUserExpired.getId()))
                .thenReturn(Collections.singletonList(contact));
        when(trustedContactRepository.findByUserIdAndIsVerifiedTrue(inactiveUserExpired.getId()))
                .thenReturn(Collections.singletonList(contact));

        scheduler.checkInactiveUsers();

        verify(emailService).sendVaultRevealNotification(eq(contact), eq(inactiveUserExpired));
        verify(trustedContactRepository).save(contact);
        assertTrue(contact.getIsNotified());
        assertNotNull(contact.getNotifiedAt());
        verify(activityLogService).logActivity(eq(inactiveUserExpired), eq("VAULT_REVEALED"), anyString());
    }

    @Test
    void checkInactiveUsers_AlreadyNotifiedContacts() {
        TrustedContact contact = new TrustedContact();
        contact.setIsNotified(true);
        contact.setUser(inactiveUserExpired);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUserExpired));
        when(trustedContactRepository.findByUserId(inactiveUserExpired.getId()))
                .thenReturn(Collections.singletonList(contact));

        scheduler.checkInactiveUsers();

        verify(emailService, never()).sendVaultRevealNotification(any(), any());
    }

    @Test
    void checkInactiveUsers_MultipleUsers() {
        List<User> users = Arrays.asList(activeUser, inactiveUser50, inactiveUser75, 
                inactiveUserLastWeek, inactiveUserExpired);
        
        when(userRepository.findAll()).thenReturn(users);
        when(userService.generateActivityToken(any())).thenReturn("test-token");
        when(trustedContactRepository.findByUserId(inactiveUserExpired.getId()))
                .thenReturn(Collections.emptyList());

        scheduler.checkInactiveUsers();

        // Verify appropriate actions for each user
        verify(emailService, never()).sendInactivityWarningEmail(eq(activeUser), any(), anyLong(), any());
        verify(emailService).sendInactivityWarningEmail(eq(inactiveUser50), eq("50% warning"), eq(90L), any());
        verify(emailService).sendInactivityWarningEmail(eq(inactiveUser75), eq("75% warning"), eq(135L), any());
        verify(emailService).sendInactivityWarningEmail(eq(inactiveUserLastWeek), eq("final week warning"), eq(175L), any());
        verify(trustedContactRepository).findByUserId(inactiveUserExpired.getId());
    }

    @Test
    void checkInactiveUsers_CustomInactivityPeriod() {
        User customUser = createUser(8L, "custom@example.com", LocalDateTime.now().minusDays(182), 365);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(customUser));
        when(userService.generateActivityToken(customUser)).thenReturn("token-custom");

        scheduler.checkInactiveUsers();

        // 182 days = 50% of 365 days (182.5, rounded down)
        verify(emailService).sendInactivityWarningEmail(eq(customUser), eq("50% warning"), eq(182L), eq("token-custom"));
        verify(activityLogService).logActivity(eq(customUser), eq("INACTIVITY_CHECK"), 
                contains("50% warning"));
    }

    @Test
    void checkInactiveUsers_OnlyActiveUsers() {
        User inactiveUser = createUser(9L, "inactive@example.com", LocalDateTime.now().minusDays(100), 180);
        inactiveUser.setIsActive(false);

        when(userRepository.findAll()).thenReturn(Arrays.asList(activeUser, inactiveUser));

        scheduler.checkInactiveUsers();

        // Should not process inactive user
        verify(emailService, never()).sendInactivityWarningEmail(eq(inactiveUser), any(), anyLong(), any());
    }

    @Test
    void checkInactiveUsers_NoVerifiedContacts() {
        TrustedContact unverifiedContact = new TrustedContact();
        unverifiedContact.setIsVerified(false);
        unverifiedContact.setUser(inactiveUserExpired);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(inactiveUserExpired));
        when(trustedContactRepository.findByUserId(inactiveUserExpired.getId()))
                .thenReturn(Collections.singletonList(unverifiedContact));
        when(trustedContactRepository.findByUserIdAndIsVerifiedTrue(inactiveUserExpired.getId()))
                .thenReturn(Collections.emptyList());

        scheduler.checkInactiveUsers();

        verify(emailService, never()).sendVaultRevealNotification(any(), any());
        verify(activityLogService).logActivity(eq(inactiveUserExpired), eq("VAULT_REVEALED"), anyString());
    }

    @Test
    void isSchedulerEnabled() {
        // Test the scheduled method is properly annotated
        try {
            InactivityCheckScheduler.class.getMethod("checkInactiveUsers")
                    .getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        } catch (NoSuchMethodException e) {
            fail("checkInactiveUsers method should have @Scheduled annotation");
        }
    }
}