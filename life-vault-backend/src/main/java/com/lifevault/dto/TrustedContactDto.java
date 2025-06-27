package com.lifevault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrustedContactDto {
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phoneNumber;
    
    private String address;
    
    @NotBlank
    private String relationship;
    
    private Boolean isVerified;
    
    private LocalDateTime verifiedAt;
    
    private Boolean isNotified;
    
    private LocalDateTime notifiedAt;
    
    private LocalDateTime createdAt;
}