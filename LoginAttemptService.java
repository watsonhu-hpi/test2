package com.chatapp.service;

import com.chatapp.model.LoginAttempt;
import com.chatapp.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    public void saveLoginAttempt(String username, String password, String email, 
                                String ipAddress, String userAgent, boolean successful) {
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .password(password)
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(successful)
                .build();
        
        loginAttemptRepository.save(attempt);
    }
}