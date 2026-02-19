package com.kkvat.automation.service;

import com.kkvat.automation.dto.PasswordChangeRequest;
import com.kkvat.automation.dto.UserRequest;
import com.kkvat.automation.dto.UserResponse;
import com.kkvat.automation.exception.BadRequestException;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.from(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return UserResponse.from(user);
    }
    
    @Transactional
    public UserResponse createUser(UserRequest request, Long createdBy) {
        log.debug("Creating new user: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }
        
        // Map incoming role string to enum, default to VIEWER
        User.Role roleEnum = User.Role.VIEWER;
        if (request.getRole() != null) {
            try {
                roleEnum = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(roleEnum)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .passwordExpiresAt(LocalDateTime.now().plusDays(90))
                .build();
        
        User savedUser = userRepository.save(user);
        
        auditService.logSuccess(
                "CREATE_USER",
                "User",
                savedUser.getId(),
                "Created user: " + savedUser.getUsername()
        );
        
        log.info("User created successfully: {}", savedUser.getUsername());
        return UserResponse.from(savedUser);
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request, Long updatedBy) {
        log.debug("Updating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if username is being changed and if it already exists
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new BadRequestException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        if (request.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
        }
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        }
        
        User updatedUser = userRepository.save(user);
        
        auditService.logSuccess(
                "UPDATE_USER",
                "User",
                updatedUser.getId(),
                "Updated user: " + updatedUser.getUsername()
        );
        
        log.info("User updated successfully: {}", updatedUser.getUsername());
        return UserResponse.from(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id, Long deletedBy) {
        log.debug("Deleting user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        String username = user.getUsername();
        userRepository.delete(user);
        
        auditService.logSuccess(
                "DELETE_USER",
                "User",
                id,
                "Deleted user: " + username
        );
        
        log.info("User deleted successfully: {}", username);
    }
    
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.debug("Changing password for user id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        userRepository.save(user);
        
        auditService.logSuccess(
                "CHANGE_PASSWORD",
                "User",
                userId,
                "Password changed for user: " + user.getUsername()
        );
        
        log.info("Password changed successfully for user: {}", user.getUsername());
    }
    
    @Transactional
    public void resetPassword(Long userId, String newPassword, Long resetBy) {
        log.debug("Resetting password for user id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        user.setFailedLoginAttempts(0);
        user.setIsLocked(false);
        userRepository.save(user);
        
        auditService.logSuccess(
                "RESET_PASSWORD",
                "User",
                userId,
                "Password reset for user: " + user.getUsername()
        );
        
        log.info("Password reset successfully for user: {}", user.getUsername());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        log.debug("Searching users with keyword: {}", keyword);
        return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.debug("Fetching active users");
        return userRepository.findByIsActive(true)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
