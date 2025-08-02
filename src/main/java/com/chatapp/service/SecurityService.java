package com.chatapp.service;

import com.chatapp.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for handling fine-grained permissions beyond role-based access
 */
@Service
public class SecurityService {

    // Map of users with special permissions
    private final Map<String, Set<String>> userPermissions = new HashMap<>();
    
    public SecurityService() {
        // Initialize with super admin that has all permissions
        userPermissions.put("admin", Set.of(
            "VIEW_CREDENTIALS", 
            "DECRYPT_CREDENTIALS", 
            "MANAGE_CREDENTIALS"
        ));
        
        // Security officer can view but not decrypt
        userPermissions.put("securityofficer", Set.of(
            "VIEW_CREDENTIALS",
            "MANAGE_CREDENTIALS"
        ));
    }
    
    /**
     * Check if the current user has a specific permission
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String username = authentication.getName();
        
        // If it's the system admin (hardcoded admin), grant all permissions
        if ("admin".equals(username)) {
            return true;
        }
        
        // Check the user's specific permissions
        Set<String> permissions = userPermissions.get(username);
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Grant a permission to a specific user
     */
    public void grantPermission(String username, String permission) {
        userPermissions.computeIfAbsent(username, k -> Set.of())
                      .add(permission);
    }
    
    /**
     * Revoke a permission from a specific user
     */
    public void revokePermission(String username, String permission) {
        Set<String> permissions = userPermissions.get(username);
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
}