package com.lifevault.controller;

import com.lifevault.entity.User;
import com.lifevault.repository.UserRepository;
import com.lifevault.service.UserService;
import com.lifevault.scheduler.DemoInactivityCheckScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Profile("demo")
public class DemoController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private DemoInactivityCheckScheduler demoScheduler;
    
    @PostMapping("/simulate-inactivity")
    public ResponseEntity<Map<String, String>> simulateInactivity(
            Authentication auth,
            @RequestParam(defaultValue = "135") int daysInactive) {
        
        User user = userService.getUserByEmail(auth.getName());
        LocalDateTime newDate = LocalDateTime.now().minusDays(daysInactive);
        user.setLastActivityAt(newDate);
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("lastActivityDate", newDate.toString());
        response.put("daysInactive", String.valueOf(daysInactive));
        response.put("message", "Last activity date updated. Run trigger-check to process notifications.");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/trigger-check")
    public ResponseEntity<Map<String, String>> triggerCheck() {
        try {
            if (demoScheduler != null) {
                demoScheduler.checkInactiveUsers();
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "triggered");
                response.put("message", "Demo inactivity check triggered. Check logs for simulated notifications (emails disabled in demo).");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Demo scheduler not available. Ensure demo profile is active.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error triggering check: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            
            // Log the full stack trace
            e.printStackTrace();
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDemoStatus(Authentication auth) {
        User user = userService.getUserByEmail(auth.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("demoMode", true);
        response.put("schedulerInterval", "Daily at midnight (demo mode active)");
        response.put("userEmail", user.getEmail());
        response.put("lastActivityDate", user.getLastActivityAt());
        response.put("inactivityPeriodDays", user.getInactivityPeriodDays());
        
        if (user.getLastActivityAt() != null) {
            long daysInactive = java.time.temporal.ChronoUnit.DAYS.between(
                user.getLastActivityAt().toLocalDate(), LocalDateTime.now().toLocalDate());
            response.put("currentDaysInactive", daysInactive);
        }
        
        return ResponseEntity.ok(response);
    }
}