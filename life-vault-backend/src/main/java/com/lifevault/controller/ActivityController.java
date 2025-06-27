package com.lifevault.controller;

import com.lifevault.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@CrossOrigin
public class ActivityController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyActivity(@PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean verified = userService.verifyUserActivity(token);
            if (verified) {
                response.put("status", "success");
                response.put("message", "Thank you! Your activity has been confirmed. Your inactivity timer has been reset.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid or expired verification link.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while verifying your activity.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}