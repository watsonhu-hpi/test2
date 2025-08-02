package com.chatapp.service;

import com.chatapp.model.Role;
import com.chatapp.model.User;
import com.chatapp.model.dto.UserDto;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final Path fileStorageLocation = Paths.get("uploads/profile-pictures").toAbsolutePath().normalize();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserService() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public UserDto findDtoById(Long id) {
        return convertToDto(findById(id));
    }

    public List<UserDto> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(Long id, Map<String, Object> updates) {
        User user = findById(id);
        
        if (updates.containsKey("status")) {
            user.setStatus((String) updates.get("status"));
        }
        
        if (updates.containsKey("profilePicture")) {
            user.setProfilePicture((String) updates.get("profilePicture"));
        }
        
        return convertToDto(userRepository.save(user));
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        User user = findById(id);
        user.setStatus(status);
        userRepository.save(user);
    }

    @Transactional
    public void updateLastActive(Long id) {
        User user = findById(id);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public String uploadProfilePicture(Long id, MultipartFile file) {
        User user = findById(id);
        
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = fileStorageLocation.resolve(fileName);
        
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/profile-pictures/" + fileName;
            user.setProfilePicture(fileUrl);
            userRepository.save(user);
            
            return fileUrl;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName, ex);
        }
    }

    @Transactional
    public List<UserDto> findContacts(Long userId) {
        return userRepository.findContactsOf(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addContact(Long userId, Long contactId) {
        User user = findById(userId);
        User contact = findById(contactId);
        
        user.getContacts().add(contact);
        userRepository.save(user);
    }

    @Transactional
    public void removeContact(Long userId, Long contactId) {
        User user = findById(userId);
        User contact = findById(contactId);
        
        user.getContacts().remove(contact);
        userRepository.save(user);
    }

    @Transactional
    public List<UserDto> findBlockedUsers(Long userId) {
        return userRepository.findBlockedByUser(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        User user = findById(userId);
        User blockedUser = findById(blockedUserId);
        
        user.getBlockedUsers().add(blockedUser);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        User user = findById(userId);
        User blockedUser = findById(blockedUserId);
        
        user.getBlockedUsers().remove(blockedUser);
        userRepository.save(user);
    }

    public UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setStatus(user.getStatus());
        
        if (user.getLastActive() != null) {
            dto.setLastActive(user.getLastActive().format(formatter));
        }
        
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().format(formatter));
        }
        
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        dto.setRoles(roles);
        
        return dto;
    }
}