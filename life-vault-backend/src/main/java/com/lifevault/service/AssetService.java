package com.lifevault.service;

import com.lifevault.dto.AssetDto;
import com.lifevault.entity.Asset;
import com.lifevault.entity.User;
import com.lifevault.repository.AssetRepository;
import com.lifevault.repository.UserRepository;
import com.lifevault.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssetService {
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EncryptionUtil encryptionUtil;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    public List<AssetDto> getUserAssets(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Asset> assets = assetRepository.findByUserIdAndIsActiveTrue(user.getId());
        
        return assets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public AssetDto createAsset(String userEmail, AssetDto assetDto) {
        User user = getUserByEmail(userEmail);
        
        Asset asset = new Asset();
        asset.setUser(user);
        asset.setName(assetDto.getName());
        asset.setDescription(assetDto.getDescription());
        asset.setType(assetDto.getType());
        asset.setInstitution(assetDto.getInstitution());
        asset.setLocation(assetDto.getLocation());
        
        if (assetDto.getNotes() != null && !assetDto.getNotes().isEmpty()) {
            asset.setEncryptedNotes(encryptionUtil.encrypt(assetDto.getNotes()));
        }
        
        Asset savedAsset = assetRepository.save(asset);
        activityLogService.logActivity(user, "ASSET_CREATED", "Created asset: " + asset.getName());
        
        return convertToDto(savedAsset);
    }
    
    public AssetDto updateAsset(String userEmail, Long assetId, AssetDto assetDto) {
        User user = getUserByEmail(userEmail);
        Asset asset = assetRepository.findByIdAndUserId(assetId, user.getId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        
        asset.setName(assetDto.getName());
        asset.setDescription(assetDto.getDescription());
        asset.setType(assetDto.getType());
        asset.setInstitution(assetDto.getInstitution());
        asset.setLocation(assetDto.getLocation());
        
        if (assetDto.getNotes() != null && !assetDto.getNotes().isEmpty()) {
            asset.setEncryptedNotes(encryptionUtil.encrypt(assetDto.getNotes()));
        }
        
        Asset savedAsset = assetRepository.save(asset);
        activityLogService.logActivity(user, "ASSET_UPDATED", "Updated asset: " + asset.getName());
        
        return convertToDto(savedAsset);
    }
    
    public void deleteAsset(String userEmail, Long assetId) {
        User user = getUserByEmail(userEmail);
        Asset asset = assetRepository.findByIdAndUserId(assetId, user.getId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        
        asset.setIsActive(false);
        assetRepository.save(asset);
        
        activityLogService.logActivity(user, "ASSET_DELETED", "Deleted asset: " + asset.getName());
    }
    
    private AssetDto convertToDto(Asset asset) {
        AssetDto dto = new AssetDto();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setDescription(asset.getDescription());
        dto.setType(asset.getType());
        dto.setInstitution(asset.getInstitution());
        dto.setLocation(asset.getLocation());
        
        if (asset.getEncryptedNotes() != null) {
            dto.setNotes(encryptionUtil.decrypt(asset.getEncryptedNotes()));
        }
        
        dto.setCreatedAt(asset.getCreatedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        dto.setIsActive(asset.getIsActive());
        
        return dto;
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}