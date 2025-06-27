package com.lifevault.service;

import com.lifevault.dto.AssetDto;
import com.lifevault.entity.Asset;
import com.lifevault.entity.User;
import com.lifevault.repository.AssetRepository;
import com.lifevault.repository.UserRepository;
import com.lifevault.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private AssetService assetService;

    private User testUser;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testAsset = new Asset();
        testAsset.setId(1L);
        testAsset.setName("Test Bank Account");
        testAsset.setType(Asset.AssetType.BANK_ACCOUNT);
        testAsset.setUser(testUser);
        testAsset.setIsActive(true);
    }

    @Test
    void getUserAssets_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(assetRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Arrays.asList(testAsset));

        List<AssetDto> result = assetService.getUserAssets("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Bank Account", result.get(0).getName());
        verify(assetRepository).findByUserIdAndIsActiveTrue(1L);
    }

    @Test
    void createAsset_Success() {
        AssetDto assetDto = new AssetDto();
        assetDto.setName("New Asset");
        assetDto.setType(Asset.AssetType.INVESTMENT);
        assetDto.setNotes("Private notes");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(encryptionUtil.encrypt("Private notes")).thenReturn("encrypted-notes");
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        AssetDto result = assetService.createAsset("test@example.com", assetDto);

        assertNotNull(result);
        assertEquals("New Asset", result.getName());
        assertEquals(Asset.AssetType.INVESTMENT, result.getType());
        verify(encryptionUtil).encrypt("Private notes");
        verify(assetRepository).save(any(Asset.class));
        verify(activityLogService).logActivity(eq(testUser), eq("ASSET_CREATED"), anyString());
    }

    @Test
    void updateAsset_Success() {
        AssetDto updateDto = new AssetDto();
        updateDto.setName("Updated Asset");
        updateDto.setType(Asset.AssetType.CRYPTO);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(assetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        AssetDto result = assetService.updateAsset("test@example.com", 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Asset", testAsset.getName());
        assertEquals(Asset.AssetType.CRYPTO, testAsset.getType());
        verify(assetRepository).save(testAsset);
        verify(activityLogService).logActivity(eq(testUser), eq("ASSET_UPDATED"), anyString());
    }

    @Test
    void updateAsset_NotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(assetRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        AssetDto updateDto = new AssetDto();
        assertThrows(RuntimeException.class, () -> {
            assetService.updateAsset("test@example.com", 999L, updateDto);
        });
    }

    @Test
    void deleteAsset_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(assetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);

        assetService.deleteAsset("test@example.com", 1L);

        assertFalse(testAsset.getIsActive());
        verify(assetRepository).save(testAsset);
        verify(activityLogService).logActivity(eq(testUser), eq("ASSET_DELETED"), anyString());
    }

    @Test
    void encryptionDecryption_Integration() {
        testAsset.setEncryptedNotes("encrypted-data");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(assetRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(Arrays.asList(testAsset));
        when(encryptionUtil.decrypt("encrypted-data")).thenReturn("decrypted notes");

        List<AssetDto> result = assetService.getUserAssets("test@example.com");

        assertEquals("decrypted notes", result.get(0).getNotes());
        verify(encryptionUtil).decrypt("encrypted-data");
    }
}