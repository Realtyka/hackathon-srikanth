package com.lifevault.service;

import com.lifevault.dto.TrustedContactDto;
import com.lifevault.entity.TrustedContact;
import com.lifevault.entity.User;
import com.lifevault.repository.TrustedContactRepository;
import com.lifevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrustedContactService {
    
    @Autowired
    private TrustedContactRepository trustedContactRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ActivityLogService activityLogService;
    
    public List<TrustedContactDto> getUserContacts(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<TrustedContact> contacts = trustedContactRepository.findByUserId(user.getId());
        
        return contacts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TrustedContactDto addContact(String userEmail, TrustedContactDto contactDto) {
        User user = getUserByEmail(userEmail);
        
        if (trustedContactRepository.existsByUserIdAndEmail(user.getId(), contactDto.getEmail())) {
            throw new RuntimeException("Contact with this email already exists");
        }
        
        TrustedContact contact = new TrustedContact();
        contact.setUser(user);
        contact.setName(contactDto.getName());
        contact.setEmail(contactDto.getEmail());
        contact.setPhoneNumber(contactDto.getPhoneNumber());
        contact.setAddress(contactDto.getAddress());
        contact.setRelationship(contactDto.getRelationship());
        
        // Mark as verified immediately since we're not sending verification emails
        contact.setIsVerified(true);
        contact.setVerifiedAt(java.time.LocalDateTime.now());
        
        TrustedContact savedContact = trustedContactRepository.save(contact);
        
        // No email sent - contacts won't know they've been added
        activityLogService.logActivity(user, "CONTACT_ADDED", "Added trusted contact: " + contact.getName());
        
        return convertToDto(savedContact);
    }
    
    public void removeContact(String userEmail, Long contactId) {
        User user = getUserByEmail(userEmail);
        TrustedContact contact = trustedContactRepository.findByIdAndUserId(contactId, user.getId())
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        trustedContactRepository.delete(contact);
        activityLogService.logActivity(user, "CONTACT_REMOVED", "Removed trusted contact: " + contact.getName());
    }
    
    public boolean verifyContact(String token) {
        TrustedContact contact = trustedContactRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (contact.getIsVerified()) {
            return true;
        }
        
        contact.setIsVerified(true);
        contact.setVerifiedAt(LocalDateTime.now());
        trustedContactRepository.save(contact);
        
        activityLogService.logActivity(contact.getUser(), "CONTACT_VERIFIED", 
                "Contact verified: " + contact.getName());
        
        return true;
    }
    
    private TrustedContactDto convertToDto(TrustedContact contact) {
        TrustedContactDto dto = new TrustedContactDto();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setPhoneNumber(contact.getPhoneNumber());
        dto.setAddress(contact.getAddress());
        dto.setRelationship(contact.getRelationship());
        dto.setIsVerified(contact.getIsVerified());
        dto.setVerifiedAt(contact.getVerifiedAt());
        dto.setIsNotified(contact.getIsNotified());
        dto.setNotifiedAt(contact.getNotifiedAt());
        dto.setCreatedAt(contact.getCreatedAt());
        
        return dto;
    }
    
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}