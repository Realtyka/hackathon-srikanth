package com.lifevault.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;
    
    @Column(nullable = false)
    private String description;
    
    @Column
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ActivityType {
        LOGIN,
        LOGOUT,
        ASSET_CREATED,
        ASSET_UPDATED,
        ASSET_DELETED,
        CONTACT_ADDED,
        CONTACT_REMOVED,
        CONTACT_VERIFIED,
        INACTIVITY_CHECK,
        NOTIFICATION_SENT,
        VAULT_REVEALED,
        SETTINGS_UPDATED
    }
}