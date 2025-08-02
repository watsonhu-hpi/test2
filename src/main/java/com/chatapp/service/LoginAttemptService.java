package com.chatapp.service;

import com.chatapp.model.LoginAttempt;
import com.chatapp.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class LoginAttemptService {
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @Value("${chatapp.app.jwtSecret:chatAppSecretKey}")
    private String encryptionKey;
    
    private TextEncryptor encryptor;
    
    @PostConstruct
    public void init() {
        // Using a different salt than the JWT secret for better security
        String salt = Base64.getEncoder().encodeToString("credentialSalt".getBytes()).substring(0, 8);
        encryptor = Encryptors.text(encryptionKey, salt);
    }
    
    public void saveLoginAttempt(String username, String password, String email, 
                                String ipAddress, String userAgent, boolean successful) {
        // Encrypt sensitive data before storing
        String encryptedPassword = password != null ? encryptor.encrypt(password) : null;
        String encryptedEmail = email != null ? encryptor.encrypt(email) : null;
        
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .password(encryptedPassword)
                .email(encryptedEmail)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(successful)
                .build();
        
        loginAttemptRepository.save(attempt);
    }
    
    /**
     * Scheduled task to delete credential records older than 30 days
     * Runs at midnight every day
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void purgeOldCredentials() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<LoginAttempt> oldRecords = loginAttemptRepository.findByAttemptTimeBefore(cutoffDate);
        loginAttemptRepository.deleteAll(oldRecords);
    }
    
    /**
     * Decrypt a password for authorized viewing
     */
    public String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null) return null;
        return encryptor.decrypt(encryptedPassword);
    }
    
    /**
     * Decrypt an email for authorized viewing
     */
    public String decryptEmail(String encryptedEmail) {
        if (encryptedEmail == null) return null;
        return encryptor.decrypt(encryptedEmail);
    }
}