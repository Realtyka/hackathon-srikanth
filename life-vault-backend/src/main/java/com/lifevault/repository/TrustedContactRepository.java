package com.lifevault.repository;

import com.lifevault.entity.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedContactRepository extends JpaRepository<TrustedContact, Long> {
    
    List<TrustedContact> findByUserId(Long userId);
    
    Optional<TrustedContact> findByIdAndUserId(Long id, Long userId);
    
    Optional<TrustedContact> findByVerificationToken(String token);
    
    boolean existsByUserIdAndEmail(Long userId, String email);
    
    List<TrustedContact> findByUserIdAndIsVerifiedTrue(Long userId);
}