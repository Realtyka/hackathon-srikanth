package com.lifevault.service;

import com.lifevault.dto.ChangePasswordRequest;
import com.lifevault.dto.UpdateProfileRequest;
import com.lifevault.dto.UserSettingsRequest;
import com.lifevault.entity.User;
import com.lifevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        
        userRepository.save(user);
        activityLogService.logActivity(user, "SETTINGS_UPDATED", "Profile information updated");
    }
    
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        activityLogService.logActivity(user, "SETTINGS_UPDATED", "Password changed");
    }
    
    public void updateSettings(String email, UserSettingsRequest request) {
        User user = getUserByEmail(email);
        
        user.setInactivityPeriodDays(request.getInactivityPeriodDays());
        userRepository.save(user);
        
        activityLogService.logActivity(user, "SETTINGS_UPDATED", 
                "Inactivity period updated to " + request.getInactivityPeriodDays() + " days");
    }
    
    public void updateLastActivity(String email) {
        User user = getUserByEmail(email);
        user.setLastActivityAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public String generateActivityToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setActivityVerificationToken(token);
        user.setTokenExpiryDate(LocalDateTime.now().plusDays(7)); // Token valid for 7 days
        userRepository.save(user);
        return token;
    }
    
    public boolean verifyUserActivity(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getActivityVerificationToken()))
                .filter(u -> u.getTokenExpiryDate() != null && u.getTokenExpiryDate().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);
        
        if (user != null) {
            // Reset activity timer
            user.setLastActivityAt(LocalDateTime.now());
            user.setLastNotificationCheckAt(LocalDateTime.now());
            // Clear the token after use
            user.setActivityVerificationToken(null);
            user.setTokenExpiryDate(null);
            userRepository.save(user);
            
            activityLogService.logActivity(user, "INACTIVITY_CHECK", 
                    "User confirmed activity via email link");
            
            return true;
        }
        
        return false;
    }
}