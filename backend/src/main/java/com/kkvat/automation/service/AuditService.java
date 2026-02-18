package com.kkvat.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkvat.automation.model.AuditLog;
import com.kkvat.automation.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Async
    @Transactional
    public void logAction(String action, String resourceType, Long resourceId, Object details, AuditLog.Status status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            Long userId = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof com.kkvat.automation.security.UserPrincipal) {
                com.kkvat.automation.security.UserPrincipal userPrincipal = 
                        (com.kkvat.automation.security.UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getId();
            }
            
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;
            
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(detailsJson)
                    .ipAddress(getClientIp())
                    .userAgent(request.getHeader("User-Agent"))
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            auditLogRepository.save(auditLog);
            
            log.info("Audit log created: {} - {} - {} - {}", username, action, resourceType, status);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }
    
    public void logSuccess(String action, String resourceType, Long resourceId, Object details) {
        logAction(action, resourceType, resourceId, details, AuditLog.Status.SUCCESS);
    }
    
    public void logFailure(String action, String resourceType, Long resourceId, Object details) {
        logAction(action, resourceType, resourceId, details, AuditLog.Status.FAILURE);
    }
    
    public void logWarning(String action, String resourceType, Long resourceId, Object details) {
        logAction(action, resourceType, resourceId, details, AuditLog.Status.WARNING);
    }
    
    private String getClientIp() {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
}
