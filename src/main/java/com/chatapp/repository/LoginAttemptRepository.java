package com.chatapp.repository;

import com.chatapp.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUsernameAndSuccessful(String username, boolean successful);
    List<LoginAttempt> findByIpAddressAndAttemptTimeBetween(String ipAddress, LocalDateTime start, LocalDateTime end);
    List<LoginAttempt> findBySuccessfulOrderByAttemptTimeDesc(boolean successful);
    List<LoginAttempt> findByAttemptTimeBefore(LocalDateTime cutoffDate);
}