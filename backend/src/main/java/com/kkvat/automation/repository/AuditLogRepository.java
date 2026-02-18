package com.kkvat.automation.repository;

import com.kkvat.automation.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);
    
    Page<AuditLog> findByStatus(AuditLog.Status status, Pageable pageable);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    Page<AuditLog> findByUserIdAndTimestampBetween(
            Long userId, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable
    );
}
