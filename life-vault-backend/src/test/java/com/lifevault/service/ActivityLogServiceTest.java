package com.lifevault.service;

import com.lifevault.entity.ActivityLog;
import com.lifevault.entity.User;
import com.lifevault.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void logActivity_Success() {
        String activityType = "LOGIN";
        String description = "User logged in successfully";

        activityLogService.logActivity(testUser, activityType, description);

        ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());

        ActivityLog savedLog = logCaptor.getValue();
        assertEquals(testUser, savedLog.getUser());
        assertEquals(ActivityLog.ActivityType.LOGIN, savedLog.getType());
        assertEquals(description, savedLog.getDescription());
        assertNull(savedLog.getIpAddress());
    }

    @Test
    void logActivity_WithIpAddress() {
        String activityType = "LOGIN";
        String description = "User logged in successfully";
        String ipAddress = "192.168.1.1";

        activityLogService.logActivity(testUser, activityType, description, ipAddress);

        ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(logCaptor.capture());

        ActivityLog savedLog = logCaptor.getValue();
        assertEquals(testUser, savedLog.getUser());
        assertEquals(ActivityLog.ActivityType.LOGIN, savedLog.getType());
        assertEquals(description, savedLog.getDescription());
        assertEquals(ipAddress, savedLog.getIpAddress());
    }

    @Test
    void getUserActivityLogs_Success() {
        ActivityLog log1 = new ActivityLog();
        log1.setId(1L);
        log1.setUser(testUser);
        log1.setType(ActivityLog.ActivityType.LOGIN);
        log1.setDescription("Login activity");
        log1.setCreatedAt(LocalDateTime.now().minusHours(2));

        ActivityLog log2 = new ActivityLog();
        log2.setId(2L);
        log2.setUser(testUser);
        log2.setType(ActivityLog.ActivityType.ASSET_CREATED);
        log2.setDescription("Asset created");
        log2.setCreatedAt(LocalDateTime.now().minusHours(1));

        List<ActivityLog> logs = Arrays.asList(log2, log1); // Ordered by createdAt desc
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityLog> page = new PageImpl<>(logs, pageable, logs.size());

        when(activityLogRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(page);

        Page<ActivityLog> result = activityLogService.getUserActivityLogs(1L, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(ActivityLog.ActivityType.ASSET_CREATED, result.getContent().get(0).getType()); // Most recent first
        assertEquals(ActivityLog.ActivityType.LOGIN, result.getContent().get(1).getType());
        verify(activityLogRepository).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    void getUserActivityLogs_EmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityLog> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(activityLogRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(emptyPage);

        Page<ActivityLog> result = activityLogService.getUserActivityLogs(1L, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void logActivity_ExceptionHandling() {
        doThrow(new RuntimeException("Database error"))
                .when(activityLogRepository).save(any(ActivityLog.class));

        // Should throw exception when database fails
        assertThrows(RuntimeException.class, () -> 
            activityLogService.logActivity(testUser, "LOGIN", "Test login")
        );

        verify(activityLogRepository).save(any(ActivityLog.class));
    }

    @Test
    void logActivity_VariousActivityTypes() {
        // Test various activity types
        ActivityLog.ActivityType[] types = ActivityLog.ActivityType.values();

        for (ActivityLog.ActivityType type : types) {
            activityLogService.logActivity(testUser, type.name(), "Test " + type.name());
        }

        verify(activityLogRepository, times(types.length)).save(any(ActivityLog.class));
    }

    @Test
    void logActivity_InvalidActivityType() {
        // Test with invalid activity type
        assertThrows(IllegalArgumentException.class, () -> {
            activityLogService.logActivity(testUser, "INVALID_TYPE", "Test description");
        });
    }
}