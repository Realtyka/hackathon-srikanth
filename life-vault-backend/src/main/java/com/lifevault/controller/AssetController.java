package com.lifevault.controller;

import com.lifevault.dto.AssetDto;
import com.lifevault.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin
public class AssetController {
    
    @Autowired
    private AssetService assetService;
    
    @GetMapping
    public ResponseEntity<List<AssetDto>> getUserAssets(Authentication authentication) {
        List<AssetDto> assets = assetService.getUserAssets(authentication.getName());
        return ResponseEntity.ok(assets);
    }
    
    @PostMapping
    public ResponseEntity<AssetDto> createAsset(Authentication authentication,
                                               @Valid @RequestBody AssetDto assetDto) {
        AssetDto createdAsset = assetService.createAsset(authentication.getName(), assetDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAsset);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AssetDto> updateAsset(Authentication authentication,
                                               @PathVariable Long id,
                                               @Valid @RequestBody AssetDto assetDto) {
        AssetDto updatedAsset = assetService.updateAsset(authentication.getName(), id, assetDto);
        return ResponseEntity.ok(updatedAsset);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(Authentication authentication,
                                           @PathVariable Long id) {
        assetService.deleteAsset(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}