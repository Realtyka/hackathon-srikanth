package com.lifevault.scheduler;

import com.lifevault.entity.User;
import com.lifevault.repository.UserRepository;
import com.lifevault.service.ActivityLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("demo")
@Slf4j
public class DemoInactivityCheckScheduler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Value("${inactivity.grace-period-days:14}")
    private int gracePeriodDays;
    
    // Removed @Scheduled annotation - this will only run when manually triggered
    @Transactional
    public void checkInactiveUsers() {
        log.info("DEMO MODE: Running manually triggered inactivity check (emails disabled for demo)");
        LocalDateTime now = LocalDateTime.now();
        
        // Get all active users
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();
        
        for (User user : activeUsers) {
            LocalDateTime lastActivity = user.getLastActivityAt();
            if (lastActivity == null) {
                continue;
            }
            
            long daysSinceLastActivity = ChronoUnit.DAYS.between(lastActivity, now);
            int inactivityPeriodDays = user.getInactivityPeriodDays();
            int totalPeriodWithGrace = inactivityPeriodDays + gracePeriodDays;
            
            log.info("DEMO: Checking user {} - Days inactive: {}, Inactivity period: {} days", 
                user.getEmail(), daysSinceLastActivity, inactivityPeriodDays);
            
            // Grace period expired - would notify trusted contacts
            if (daysSinceLastActivity > totalPeriodWithGrace) {
                log.warn("DEMO: User {} exceeded grace period ({} days). In production, trusted contacts would be notified.", 
                    user.getEmail(), totalPeriodWithGrace);
                activityLogService.logActivity(user, "NOTIFICATION_SENT", 
                    "Demo: Grace period expired - contacts would be notified");
            }
            // In grace period
            else if (daysSinceLastActivity > inactivityPeriodDays) {
                long daysInGracePeriod = daysSinceLastActivity - inactivityPeriodDays;
                log.warn("DEMO: User {} is in grace period (day {} of {}). In production, reminder emails would be sent.", 
                    user.getEmail(), daysInGracePeriod, gracePeriodDays);
                activityLogService.logActivity(user, "NOTIFICATION_SENT", 
                    String.format("Demo: In grace period - day %d of %d", daysInGracePeriod, gracePeriodDays));
            }
            // Final week warning
            else if (daysSinceLastActivity > (inactivityPeriodDays - 7)) {
                long daysUntilInactive = inactivityPeriodDays - daysSinceLastActivity;
                log.info("DEMO: User {} has {} days until inactive. In production, final week warnings would be sent.", 
                    user.getEmail(), daysUntilInactive);
                activityLogService.logActivity(user, "NOTIFICATION_SENT", 
                    String.format("Demo: Final week - %d days remaining", daysUntilInactive));
            }
            // 75% warning
            else if (daysSinceLastActivity > (inactivityPeriodDays * 0.75)) {
                log.info("DEMO: User {} has passed 75% of inactivity period. In production, 75% warning would be sent.", 
                    user.getEmail());
                activityLogService.logActivity(user, "NOTIFICATION_SENT", 
                    "Demo: 75% warning would be sent");
            }
            // 50% warning
            else if (daysSinceLastActivity > (inactivityPeriodDays * 0.5)) {
                log.info("DEMO: User {} has passed 50% of inactivity period. In production, 50% warning would be sent.", 
                    user.getEmail());
                activityLogService.logActivity(user, "NOTIFICATION_SENT", 
                    "Demo: 50% warning would be sent");
            }
        }
        
        log.info("DEMO MODE: Inactivity check completed. Check activity logs for demo notifications.");
    }
}