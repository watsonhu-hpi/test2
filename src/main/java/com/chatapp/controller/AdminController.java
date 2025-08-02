package com.chatapp.controller;

import com.chatapp.model.LoginAttempt;
import com.chatapp.model.User;
import com.chatapp.repository.LoginAttemptRepository;
import com.chatapp.service.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    /**
     * Get all login attempts with additional access control and audit logging
     */
    @GetMapping("/login-attempts")
    @PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('VIEW_CREDENTIALS')")
    public ResponseEntity<List<LoginAttempt>> getAllLoginAttempts(HttpServletRequest request) {
        // Audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();
        logger.warn("Admin {} accessed all login credentials from IP {}", 
                 adminUsername, request.getRemoteAddr());
        
        // Get attempts but don't return raw passwords/emails directly
        List<LoginAttempt> attempts = loginAttemptRepository.findAll();
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * Get only successful login attempts
     */
    @GetMapping("/login-attempts/successful")
    @PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('VIEW_CREDENTIALS')")
    public ResponseEntity<List<LoginAttempt>> getSuccessfulLoginAttempts(HttpServletRequest request) {
        // Audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.warn("Admin {} accessed successful login credentials from IP {}", 
                 auth.getName(), request.getRemoteAddr());
        
        List<LoginAttempt> attempts = loginAttemptRepository.findBySuccessfulOrderByAttemptTimeDesc(true);
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * Get only failed login attempts
     */
    @GetMapping("/login-attempts/failed")
    @PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('VIEW_CREDENTIALS')")
    public ResponseEntity<List<LoginAttempt>> getFailedLoginAttempts(HttpServletRequest request) {
        // Audit logging
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.warn("Admin {} accessed failed login credentials from IP {}", 
                 auth.getName(), request.getRemoteAddr());
        
        List<LoginAttempt> attempts = loginAttemptRepository.findBySuccessfulOrderByAttemptTimeDesc(false);
        return ResponseEntity.ok(attempts);
    }
    
    /**
     * Get credential with decrypted sensitive data - requires highest level permission
     */
    @GetMapping("/login-attempts/{id}/decrypt")
    @PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('DECRYPT_CREDENTIALS')")
    public ResponseEntity<?> getDecryptedCredential(@PathVariable Long id, HttpServletRequest request) {
        // Enhanced audit logging for decryption operations
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.warn("SECURITY ALERT: Admin {} decrypted credential ID {} from IP {}", 
                 auth.getName(), id, request.getRemoteAddr());
        
        LoginAttempt attempt = loginAttemptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Credential not found"));
        
        // Create DTO with decrypted values
        return ResponseEntity.ok(new LoginAttemptDTO(
            attempt.getId(),
            attempt.getUsername(),
            loginAttemptService.decryptPassword(attempt.getPassword()),
            loginAttemptService.decryptEmail(attempt.getEmail()),
            attempt.getIpAddress(),
            attempt.getUserAgent(),
            attempt.isSuccessful(),
            attempt.getAttemptTime()
        ));
    }
    
    /**
     * Delete credentials older than a specific date
     */
    @DeleteMapping("/login-attempts/purge")
    @PreAuthorize("hasRole('ADMIN') and @securityService.hasPermission('MANAGE_CREDENTIALS')")
    public ResponseEntity<?> purgeOldCredentials(@RequestParam int days, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.warn("Admin {} purged credentials older than {} days from IP {}", 
                 auth.getName(), days, request.getRemoteAddr());
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<LoginAttempt> oldRecords = loginAttemptRepository.findByAttemptTimeBefore(cutoffDate);
        loginAttemptRepository.deleteAll(oldRecords);
        
        return ResponseEntity.ok(Map.of("message", "Purged " + oldRecords.size() + " old records"));
    }
    
    /**
     * DTO for returning decrypted credential data
     */
    private static class LoginAttemptDTO {
        private Long id;
        private String username;
        private String password;
        private String email;
        private String ipAddress;
        private String userAgent;
        private boolean successful;
        private LocalDateTime attemptTime;
        
        public LoginAttemptDTO(Long id, String username, String password, String email,
                             String ipAddress, String userAgent, boolean successful,
                             LocalDateTime attemptTime) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.email = email;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.successful = successful;
            this.attemptTime = attemptTime;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getEmail() { return email; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public boolean isSuccessful() { return successful; }
        public LocalDateTime getAttemptTime() { return attemptTime; }
    }
}