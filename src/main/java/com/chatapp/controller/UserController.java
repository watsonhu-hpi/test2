package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.model.dto.UserDto;
import com.chatapp.security.UserDetailsImpl;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserDto userDto = userService.findDtoById(userDetails.getId());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.findDtoById(id);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<UserDto> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateCurrentUser(
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserDto updatedUser = userService.updateUser(userDetails.getId(), updates);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String fileUrl = userService.uploadProfilePicture(userDetails.getId(), file);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("url", fileUrl);
        }});
    }

    @GetMapping("/contacts")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getUserContacts(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserDto> contacts = userService.findContacts(userDetails.getId());
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/contacts/{contactId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addContact(
            @PathVariable Long contactId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.addContact(userDetails.getId(), contactId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Contact added successfully");
        }});
    }

    @DeleteMapping("/contacts/{contactId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> removeContact(
            @PathVariable Long contactId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.removeContact(userDetails.getId(), contactId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "Contact removed successfully");
        }});
    }

    @PostMapping("/block/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(
            @PathVariable Long userId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.blockUser(userDetails.getId(), userId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "User blocked successfully");
        }});
    }

    @DeleteMapping("/block/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(
            @PathVariable Long userId,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        userService.unblockUser(userDetails.getId(), userId);
        
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "User unblocked successfully");
        }});
    }

    @GetMapping("/blocked")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getBlockedUsers(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<UserDto> blockedUsers = userService.findBlockedUsers(userDetails.getId());
        return ResponseEntity.ok(blockedUsers);
    }
}