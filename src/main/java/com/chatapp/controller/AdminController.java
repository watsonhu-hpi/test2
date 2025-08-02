package com.chatapp.controller;

import com.chatapp.model.LoginAttempt;
import com.chatapp.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @GetMapping("/login-attempts")
    public ResponseEntity<List<LoginAttempt>> getAllLoginAttempts() {
        List<LoginAttempt> attempts = loginAttemptRepository.findAll();
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/login-attempts/successful")
    public ResponseEntity<List<LoginAttempt>> getSuccessfulLoginAttempts() {
        List<LoginAttempt> attempts = loginAttemptRepository.findBySuccessfulOrderByAttemptTimeDesc(true);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/login-attempts/failed")
    public ResponseEntity<List<LoginAttempt>> getFailedLoginAttempts() {
        List<LoginAttempt> attempts = loginAttemptRepository.findBySuccessfulOrderByAttemptTimeDesc(false);
        return ResponseEntity.ok(attempts);
    }
}