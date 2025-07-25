package com.lifevault.repository;

import com.lifevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastActivityAt < :inactivityThreshold")
    List<User> findInactiveUsers(LocalDateTime inactivityThreshold);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastNotificationCheckAt < :checkThreshold")
    List<User> findUsersForNotificationCheck(LocalDateTime checkThreshold);
}