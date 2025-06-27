package com.lifevault.scheduler;

import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import com.lifevault.repository.TrustedContactRepository;
import com.lifevault.repository.UserRepository;
import com.lifevault.service.ActivityLogService;
import com.lifevault.service.EmailService;
import com.lifevault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class InactivityCheckScheduler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TrustedContactRepository trustedContactRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private UserService userService;
    
    @Value("${inactivity.grace-period-days:14}")
    private int gracePeriodDays;
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void checkInactiveUsers() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all active users
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();
        
        for (User user : activeUsers) {
            processUserInactivityCheck(user, now);
        }
    }
    
    private void processUserInactivityCheck(User user, LocalDateTime now) {
        long daysSinceLastActivity = ChronoUnit.DAYS.between(user.getLastActivityAt(), now);
        int inactivityPeriod = user.getInactivityPeriodDays();
        
        // Calculate check points
        int halfPeriod = inactivityPeriod / 2;
        int threeQuartersPeriod = (inactivityPeriod * 3) / 4;
        int lastWeekStart = inactivityPeriod - 7;
        int finalPeriod = inactivityPeriod + gracePeriodDays;
        
        // Determine if we should send a notification
        boolean shouldNotify = false;
        String notificationType = "";
        
        if (daysSinceLastActivity == halfPeriod) {
            // First check at 50% of inactivity period
            shouldNotify = true;
            notificationType = "50% warning";
        } else if (daysSinceLastActivity == threeQuartersPeriod) {
            // Second check at 75% of inactivity period
            shouldNotify = true;
            notificationType = "75% warning";
        } else if (daysSinceLastActivity >= lastWeekStart && daysSinceLastActivity < inactivityPeriod) {
            // Daily checks in the last week
            shouldNotify = true;
            notificationType = "final week warning";
        } else if (daysSinceLastActivity >= inactivityPeriod && daysSinceLastActivity < finalPeriod) {
            // Check every 2 days during grace period
            if ((daysSinceLastActivity - inactivityPeriod) % 2 == 0) {
                shouldNotify = true;
                notificationType = "grace period warning";
            }
        } else if (daysSinceLastActivity >= finalPeriod) {
            // Time to reveal vault
            if (!hasNotifiedContacts(user)) {
                revealVaultToContacts(user);
                return;
            }
        }
        
        if (shouldNotify) {
            sendWarningNotification(user, notificationType, daysSinceLastActivity);
        }
    }
    
    private void sendWarningNotification(User user, String type, long daysInactive) {
        // Generate activity token for one-click verification
        String activityToken = userService.generateActivityToken(user);
        
        emailService.sendInactivityWarningEmail(user, type, daysInactive, activityToken);
        user.setLastNotificationCheckAt(LocalDateTime.now());
        userRepository.save(user);
        
        activityLogService.logActivity(user, "INACTIVITY_CHECK", 
                String.format("Inactivity %s sent after %d days", type, daysInactive));
    }
    
    private boolean hasNotifiedContacts(User user) {
        List<TrustedContact> contacts = trustedContactRepository.findByUserId(user.getId());
        return contacts.stream().anyMatch(TrustedContact::getIsNotified);
    }
    
    private void revealVaultToContacts(User user) {
        List<TrustedContact> verifiedContacts = trustedContactRepository
                .findByUserIdAndIsVerifiedTrue(user.getId());
        
        for (TrustedContact contact : verifiedContacts) {
            emailService.sendVaultRevealNotification(contact, user);
            contact.setIsNotified(true);
            contact.setNotifiedAt(LocalDateTime.now());
            trustedContactRepository.save(contact);
        }
        
        activityLogService.logActivity(user, "VAULT_REVEALED", 
                "Vault information revealed to trusted contacts due to extended inactivity");
    }
}