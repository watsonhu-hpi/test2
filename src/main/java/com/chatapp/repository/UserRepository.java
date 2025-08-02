package com.chatapp.repository;

import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = ?1")
    List<User> findByRole(String roleName);
    
    @Query("SELECT u FROM User u WHERE u NOT IN (SELECT c FROM User usr JOIN usr.contacts c WHERE usr.id = ?1)")
    List<User> findNonContacts(Long userId);
    
    @Query("SELECT u FROM User u JOIN u.contacts c WHERE c.id = ?1")
    List<User> findContactsOf(Long userId);
    
    @Query("SELECT u FROM User u WHERE u IN (SELECT c FROM User usr JOIN usr.blockedUsers c WHERE usr.id = ?1)")
    List<User> findBlockedByUser(Long userId);
}