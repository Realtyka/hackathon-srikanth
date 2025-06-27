package com.lifevault.controller;

import com.lifevault.dto.ChangePasswordRequest;
import com.lifevault.dto.UpdateProfileRequest;
import com.lifevault.dto.UserSettingsRequest;
import com.lifevault.entity.ActivityLog;
import com.lifevault.entity.User;
import com.lifevault.repository.ActivityLogRepository;
import com.lifevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("inactivityPeriodDays", user.getInactivityPeriodDays());
        profile.put("lastActivityAt", user.getLastActivityAt());
        
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(authentication.getName(), request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UserSettingsRequest request) {
        userService.updateSettings(authentication.getName(), request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Settings updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/activity-logs")
    public ResponseEntity<List<Map<String, Object>>> getActivityLogs(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        List<ActivityLog> logs = activityLogRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<Map<String, Object>> response = logs.stream()
            .map(log -> {
                Map<String, Object> logData = new HashMap<>();
                logData.put("id", log.getId());
                logData.put("type", log.getType().toString());
                logData.put("description", log.getDescription());
                logData.put("ipAddress", log.getIpAddress());
                logData.put("createdAt", log.getCreatedAt());
                return logData;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}