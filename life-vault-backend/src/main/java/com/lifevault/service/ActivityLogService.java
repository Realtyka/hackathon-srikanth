package com.lifevault.service;

import com.lifevault.entity.ActivityLog;
import com.lifevault.entity.User;
import com.lifevault.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityLogService {
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    public void logActivity(User user, String activityType, String description) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setType(ActivityLog.ActivityType.valueOf(activityType));
        log.setDescription(description);
        activityLogRepository.save(log);
    }
    
    public void logActivity(User user, String activityType, String description, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setType(ActivityLog.ActivityType.valueOf(activityType));
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        activityLogRepository.save(log);
    }
    
    public Page<ActivityLog> getUserActivityLogs(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}