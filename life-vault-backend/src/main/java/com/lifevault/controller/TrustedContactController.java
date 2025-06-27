package com.lifevault.controller;

import com.lifevault.dto.TrustedContactDto;
import com.lifevault.service.TrustedContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin
public class TrustedContactController {
    
    @Autowired
    private TrustedContactService trustedContactService;
    
    @GetMapping
    public ResponseEntity<List<TrustedContactDto>> getUserContacts(Authentication authentication) {
        List<TrustedContactDto> contacts = trustedContactService.getUserContacts(authentication.getName());
        return ResponseEntity.ok(contacts);
    }
    
    @PostMapping
    public ResponseEntity<TrustedContactDto> addContact(Authentication authentication,
                                                       @Valid @RequestBody TrustedContactDto contactDto) {
        TrustedContactDto createdContact = trustedContactService.addContact(authentication.getName(), contactDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeContact(Authentication authentication,
                                             @PathVariable Long id) {
        trustedContactService.removeContact(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyContact(@PathVariable String token) {
        boolean verified = trustedContactService.verifyContact(token);
        if (verified) {
            return ResponseEntity.ok("Contact verified successfully!");
        }
        return ResponseEntity.badRequest().body("Verification failed");
    }
}