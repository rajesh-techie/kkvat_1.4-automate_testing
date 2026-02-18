package com.kkvat.automation.repository;

import com.kkvat.automation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    List<User> findByIsActive(Boolean isActive);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt < :date AND u.isActive = true")
    List<User> findUsersWithExpiredPasswords(LocalDateTime date);
    
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);
    
    @Query("SELECT u FROM User u JOIN u.groups g WHERE g.id = :groupId")
    List<User> findByGroupId(Long groupId);
}
