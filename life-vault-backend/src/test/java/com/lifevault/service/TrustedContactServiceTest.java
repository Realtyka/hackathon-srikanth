package com.lifevault.service;

import com.lifevault.dto.TrustedContactDto;
import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import com.lifevault.repository.TrustedContactRepository;
import com.lifevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrustedContactServiceTest {

    @Mock
    private TrustedContactRepository trustedContactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private TrustedContactService trustedContactService;

    private User testUser;
    private TrustedContact testContact;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testContact = new TrustedContact();
        testContact.setId(1L);
        testContact.setName("John Doe");
        testContact.setEmail("john@example.com");
        testContact.setPhoneNumber("+1234567890");
        testContact.setAddress("123 Main St, City, State 12345");
        testContact.setRelationship("Friend");
        testContact.setUser(testUser);
        testContact.setIsVerified(true);
        testContact.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserContacts_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.findByUserId(1L)).thenReturn(Arrays.asList(testContact));

        List<TrustedContactDto> result = trustedContactService.getUserContacts("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertTrue(result.get(0).getIsVerified());
        verify(trustedContactRepository).findByUserId(1L);
    }

    @Test
    void addContact_Success() {
        TrustedContactDto contactDto = new TrustedContactDto();
        contactDto.setName("Jane Smith");
        contactDto.setEmail("jane@example.com");
        contactDto.setPhoneNumber("+9876543210");
        contactDto.setAddress("456 Oak Ave, Town, State 67890");
        contactDto.setRelationship("Sister");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.existsByUserIdAndEmail(1L, "jane@example.com")).thenReturn(false);
        when(trustedContactRepository.save(any(TrustedContact.class))).thenAnswer(invocation -> {
            TrustedContact saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        TrustedContactDto result = trustedContactService.addContact("test@example.com", contactDto);

        assertNotNull(result);
        assertEquals("Jane Smith", result.getName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("+9876543210", result.getPhoneNumber());
        assertEquals("Sister", result.getRelationship());
        assertTrue(result.getIsVerified()); // Should be verified immediately
        assertNotNull(result.getVerifiedAt());
        verify(trustedContactRepository).save(any(TrustedContact.class));
        verify(activityLogService).logActivity(eq(testUser), eq("CONTACT_ADDED"), anyString());
    }

    @Test
    void addContact_DuplicateEmail() {
        TrustedContactDto contactDto = new TrustedContactDto();
        contactDto.setName("Duplicate Contact");
        contactDto.setEmail("john@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.existsByUserIdAndEmail(1L, "john@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            trustedContactService.addContact("test@example.com", contactDto);
        });

        verify(trustedContactRepository, never()).save(any());
    }

    @Test
    void removeContact_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testContact));

        trustedContactService.removeContact("test@example.com", 1L);

        verify(trustedContactRepository).delete(testContact);
        verify(activityLogService).logActivity(eq(testUser), eq("CONTACT_REMOVED"), anyString());
    }

    @Test
    void removeContact_NotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            trustedContactService.removeContact("test@example.com", 999L);
        });

        verify(trustedContactRepository, never()).delete(any());
    }

    @Test
    void verifyContact_Success() {
        String token = "verification-token";
        testContact.setIsVerified(false);
        testContact.setVerificationToken(token);

        when(trustedContactRepository.findByVerificationToken(token)).thenReturn(Optional.of(testContact));
        when(trustedContactRepository.save(any(TrustedContact.class))).thenReturn(testContact);

        boolean result = trustedContactService.verifyContact(token);

        assertTrue(result);
        assertTrue(testContact.getIsVerified());
        assertNotNull(testContact.getVerifiedAt());
        verify(trustedContactRepository).save(testContact);
        verify(activityLogService).logActivity(eq(testUser), eq("CONTACT_VERIFIED"), anyString());
    }

    @Test
    void verifyContact_AlreadyVerified() {
        String token = "verification-token";
        testContact.setIsVerified(true);
        testContact.setVerificationToken(token);

        when(trustedContactRepository.findByVerificationToken(token)).thenReturn(Optional.of(testContact));

        boolean result = trustedContactService.verifyContact(token);

        assertTrue(result);
        verify(trustedContactRepository, never()).save(any());
    }

    @Test
    void verifyContact_InvalidToken() {
        when(trustedContactRepository.findByVerificationToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            trustedContactService.verifyContact("invalid-token");
        });
    }

    @Test
    void getUserContacts_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            trustedContactService.getUserContacts("notfound@example.com");
        });
    }

    @Test
    void getUserContacts_EmptyList() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(trustedContactRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        List<TrustedContactDto> result = trustedContactService.getUserContacts("test@example.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}