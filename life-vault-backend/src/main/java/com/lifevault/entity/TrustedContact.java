package com.lifevault.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    @Column
    private String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(nullable = false)
    private String relationship;
    
    @Column(nullable = false)
    private Boolean isNotified = false;
    
    @Column
    private LocalDateTime notifiedAt;
    
    @Column(nullable = false)
    private String verificationToken;
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @Column
    private LocalDateTime verifiedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        verificationToken = java.util.UUID.randomUUID().toString();
    }
}