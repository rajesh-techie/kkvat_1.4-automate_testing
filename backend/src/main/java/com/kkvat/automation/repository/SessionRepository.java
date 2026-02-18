package com.kkvat.automation.repository;

import com.kkvat.automation.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    Optional<Session> findByTokenHash(String tokenHash);
    
    List<Session> findByUserId(Long userId);
    
    List<Session> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    void deleteByExpiresAtBefore(LocalDateTime date);
    
    void deleteByUserId(Long userId);
    
    long countByUserIdAndIsActive(Long userId, Boolean isActive);
}
