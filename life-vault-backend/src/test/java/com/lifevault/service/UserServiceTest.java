package com.lifevault.service;

import com.lifevault.dto.ChangePasswordRequest;
import com.lifevault.dto.UpdateProfileRequest;
import com.lifevault.dto.UserSettingsRequest;
import com.lifevault.entity.User;
import com.lifevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setInactivityPeriodDays(180);
        testUser.setLastActivityAt(LocalDateTime.now());
        testUser.setIsActive(true);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserByEmail("notfound@example.com");
        });
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhoneNumber("+1234567890");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateProfile("test@example.com", request);

        assertEquals("Updated", testUser.getFirstName());
        assertEquals("Name", testUser.getLastName());
        assertEquals("+1234567890", testUser.getPhoneNumber());
        verify(userRepository).save(testUser);
        verify(activityLogService).logActivity(testUser, "SETTINGS_UPDATED", "Profile information updated");
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword("test@example.com", request);

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(activityLogService).logActivity(testUser, "SETTINGS_UPDATED", "Password changed");
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.changePassword("test@example.com", request);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateSettings_Success() {
        UserSettingsRequest request = new UserSettingsRequest();
        request.setInactivityPeriodDays(365);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateSettings("test@example.com", request);

        assertEquals(365, testUser.getInactivityPeriodDays());
        verify(userRepository).save(testUser);
        verify(activityLogService).logActivity(testUser, "SETTINGS_UPDATED", 
                "Inactivity period updated to 365 days");
    }

    @Test
    void generateActivityToken_Success() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = userService.generateActivityToken(testUser);

        assertNotNull(token);
        assertNotNull(testUser.getActivityVerificationToken());
        assertNotNull(testUser.getTokenExpiryDate());
        assertTrue(testUser.getTokenExpiryDate().isAfter(LocalDateTime.now()));
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyUserActivity_ValidToken() {
        String token = "valid-token";
        testUser.setActivityVerificationToken(token);
        testUser.setTokenExpiryDate(LocalDateTime.now().plusDays(1));

        when(userRepository.findAll()).thenReturn(java.util.Arrays.asList(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        boolean result = userService.verifyUserActivity(token);

        assertTrue(result);
        assertNull(testUser.getActivityVerificationToken());
        assertNull(testUser.getTokenExpiryDate());
        verify(userRepository).save(testUser);
        verify(activityLogService).logActivity(testUser, "INACTIVITY_CHECK", 
                "User confirmed activity via email link");
    }

    @Test
    void verifyUserActivity_ExpiredToken() {
        String token = "expired-token";
        testUser.setActivityVerificationToken(token);
        testUser.setTokenExpiryDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findAll()).thenReturn(java.util.Arrays.asList(testUser));

        boolean result = userService.verifyUserActivity(token);

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyUserActivity_InvalidToken() {
        when(userRepository.findAll()).thenReturn(java.util.Arrays.asList(testUser));

        boolean result = userService.verifyUserActivity("invalid-token");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }
}