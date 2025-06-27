#!/bin/bash

echo "Enabling demo mode for Life Vault..."
echo ""

# Check if we're in the right directory
if [ ! -d "life-vault-backend" ] || [ ! -d "life-vault-frontend" ]; then
    echo "Error: Please run this script from the life-vault root directory"
    exit 1
fi

# Create a demo version of the scheduler that runs every minute
echo "Creating demo scheduler..."
cat > life-vault-backend/src/main/java/com/lifevault/scheduler/DemoInactivityCheckScheduler.java << 'EOF'
package com.lifevault.scheduler;

import com.lifevault.entity.User;
import com.lifevault.service.EmailService;
import com.lifevault.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
@Profile("demo")
public class DemoInactivityCheckScheduler {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${inactivity.grace-period-days:30}")
    private int gracePeriodDays;
    
    // Run every minute for demo
    @Scheduled(fixedDelay = 60000)
    public void checkInactiveUsers() {
        log.info("DEMO MODE: Running inactivity check every minute");
        
        List<User> allUsers = userService.getAllActiveUsers();
        LocalDate today = LocalDate.now();
        
        for (User user : allUsers) {
            if (user.getLastActivityDate() == null) {
                continue;
            }
            
            long daysSinceLastActivity = ChronoUnit.DAYS.between(user.getLastActivityDate(), today);
            int inactivityPeriodDays = user.getInactivityPeriodDays();
            int totalPeriodWithGrace = inactivityPeriodDays + gracePeriodDays;
            
            log.info("DEMO: Checking user {} - Days inactive: {}, Period: {}", 
                user.getEmail(), daysSinceLastActivity, inactivityPeriodDays);
            
            // Grace period - notify trusted contacts
            if (daysSinceLastActivity > totalPeriodWithGrace) {
                log.info("DEMO: User {} exceeded grace period. Notifying contacts.", user.getEmail());
                emailService.notifyTrustedContacts(user);
                userService.markUserAsNotified(user);
            }
            // In grace period
            else if (daysSinceLastActivity > inactivityPeriodDays) {
                long daysInGracePeriod = daysSinceLastActivity - inactivityPeriodDays;
                if (daysInGracePeriod % 2 == 0) { // Every 2 days
                    log.info("DEMO: Sending grace period warning to {}", user.getEmail());
                    emailService.sendGracePeriodWarning(user);
                }
            }
            // Final week warning
            else if (daysSinceLastActivity > (inactivityPeriodDays - 7)) {
                log.info("DEMO: Sending final week warning to {}", user.getEmail());
                emailService.sendFinalWeekWarning(user);
            }
            // 75% warning
            else if (daysSinceLastActivity > (inactivityPeriodDays * 0.75)) {
                if (!user.getHas75PercentWarning()) {
                    log.info("DEMO: Sending 75% warning to {}", user.getEmail());
                    emailService.send75PercentWarning(user);
                    userService.mark75PercentWarningSent(user);
                }
            }
            // 50% warning
            else if (daysSinceLastActivity > (inactivityPeriodDays * 0.5)) {
                if (!user.getHas50PercentWarning()) {
                    log.info("DEMO: Sending 50% warning to {}", user.getEmail());
                    emailService.send50PercentWarning(user);
                    userService.mark50PercentWarningSent(user);
                }
            }
        }
    }
}
EOF

echo "✓ Demo scheduler created."
echo ""

# Create demo controller endpoint
echo "Creating demo controller endpoints..."
cat > life-vault-backend/src/main/java/com/lifevault/controller/DemoController.java << 'EOFCONTROLLER'
package com.lifevault.controller;

import com.lifevault.entity.User;
import com.lifevault.scheduler.InactivityCheckScheduler;
import com.lifevault.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Profile("demo")
public class DemoController {
    
    private final UserService userService;
    private final InactivityCheckScheduler scheduler;
    
    @PostMapping("/simulate-inactivity")
    public ResponseEntity<Map<String, String>> simulateInactivity(
            Authentication auth,
            @RequestParam(defaultValue = "135") int daysInactive) {
        
        User user = userService.findByEmail(auth.getName());
        LocalDate newDate = LocalDate.now().minusDays(daysInactive);
        user.setLastActivityDate(newDate);
        userService.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("lastActivityDate", newDate.toString());
        response.put("daysInactive", String.valueOf(daysInactive));
        response.put("message", "Last activity date updated. Scheduler will check on next run.");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/trigger-check")
    public ResponseEntity<Map<String, String>> triggerCheck() {
        scheduler.checkInactiveUsers();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "triggered");
        response.put("message", "Inactivity check triggered. Check logs and emails.");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDemoStatus(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("demoMode", true);
        response.put("schedulerInterval", "60 seconds");
        response.put("userEmail", user.getEmail());
        response.put("lastActivityDate", user.getLastActivityDate());
        response.put("inactivityPeriodDays", user.getInactivityPeriodDays());
        response.put("has50PercentWarning", user.getHas50PercentWarning());
        response.put("has75PercentWarning", user.getHas75PercentWarning());
        
        if (user.getLastActivityDate() != null) {
            long daysInactive = java.time.temporal.ChronoUnit.DAYS.between(
                user.getLastActivityDate(), LocalDate.now());
            response.put("currentDaysInactive", daysInactive);
        }
        
        return ResponseEntity.ok(response);
    }
}
EOFCONTROLLER

echo "✓ Demo controller created."
echo ""

# Create demo configuration
echo "Creating demo configuration..."
cat > life-vault-backend/src/main/resources/application-demo.yml << 'EOFCONFIG'
# Demo profile configuration
spring:
  profiles:
    include: local

# Override scheduler to run every minute
scheduler:
  inactivity-check:
    cron: "0 * * * * ?"  # Every minute

logging:
  level:
    com.lifevault.scheduler: DEBUG
    com.lifevault.service.EmailService: DEBUG
EOFCONFIG

echo "✓ Demo configuration created."
echo ""

# Update docker-compose to include demo profile
echo "Updating docker-compose configuration..."
if grep -q "SPRING_PROFILES_ACTIVE: local,demo" docker-compose-local.yml; then
    echo "✓ Demo profile already configured in docker-compose"
else
    sed -i.bak 's/SPRING_PROFILES_ACTIVE: local/SPRING_PROFILES_ACTIVE: local,demo/' docker-compose-local.yml
    echo "✓ Updated docker-compose to include demo profile"
fi

echo ""
echo "========================================"
echo "Demo mode setup complete!"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Rebuild and restart the application:"
echo "   docker-compose -f docker-compose-local.yml down"
echo "   docker-compose -f docker-compose-local.yml build backend"
echo "   docker-compose -f docker-compose-local.yml up -d"
echo ""
echo "2. The scheduler will run every minute in demo mode"
echo ""
echo "3. Use the demo API endpoints:"
echo "   - POST /api/demo/simulate-inactivity?daysInactive=135"
echo "   - POST /api/demo/trigger-check"
echo "   - GET /api/demo/status"
echo ""
echo "4. Or connect to the database directly:"
echo "   docker exec -it life-vault-postgres-1 psql -U postgres -d lifevault"
echo ""
echo "Example SQL commands:"
echo "  -- Simulate 50% warning (45 days before deadline)"
echo "  UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '135 days' WHERE email = 'your@email.com';"
echo ""
echo "  -- Simulate 75% warning"
echo "  UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '157 days' WHERE email = 'your@email.com';"
echo ""
echo "  -- Simulate grace period"
echo "  UPDATE users SET last_activity_date = CURRENT_DATE - INTERVAL '181 days' WHERE email = 'your@email.com';" 