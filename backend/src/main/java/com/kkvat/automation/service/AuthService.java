package com.kkvat.automation.service;

import com.kkvat.automation.dto.LoginRequest;
import com.kkvat.automation.dto.LoginResponse;
import com.kkvat.automation.exception.BadRequestException;
import com.kkvat.automation.exception.UnauthorizedException;
import com.kkvat.automation.model.Session;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.SessionRepository;
import com.kkvat.automation.repository.UserRepository;
import com.kkvat.automation.security.JwtTokenProvider;
import com.kkvat.automation.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private MenuItemService menuItemService;
    
    @Autowired
    private HttpServletRequest request;
    
    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${app.security.account.max-failed-attempts}")
    private int maxFailedAttempts;
    
    @Value("${app.security.account.lockout-duration}")
    private int lockoutDurationMinutes;
    
    @Value("${app.security.session.max-concurrent-sessions}")
    private int maxConcurrentSessions;
    
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Check if user exists
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
            
            // Check if account is locked
            if (user.getIsLocked()) {
                auditService.logFailure("LOGIN", "USER", user.getId(), "Account locked");
                throw new UnauthorizedException("Account is locked. Please contact administrator.");
            }
            
            // Check if account is active
            if (!user.getIsActive()) {
                auditService.logFailure("LOGIN", "USER", user.getId(), "Account inactive");
                throw new UnauthorizedException("Account is inactive. Please contact administrator.");
            }
            
            // Check if password is expired
            if (user.isPasswordExpired()) {
                auditService.logWarning("LOGIN", "USER", user.getId(), "Password expired");
                throw new UnauthorizedException("Password has expired. Please contact administrator to reset.");
            }
            
            // Authenticate
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );
            } catch (BadCredentialsException e) {
                handleFailedLogin(user);
                throw e;
            }
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLastFailedLogin(null);
            }
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(loginRequest.getUsername());
            
            // Handle concurrent sessions
            handleConcurrentSessions(user.getId());
            
            // Create session
            createSession(user.getId(), accessToken);
            
            // Build response
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Get menu items for the logged-in user
            java.util.List<com.kkvat.automation.dto.MenuItemDTO> userMenus = menuItemService.getMenuItemsByUserId(user.getId());
            
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // Convert to seconds
                    .user(LoginResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole().name())
                            .build())
                    .menus(userMenus)
                    .build();
            
            auditService.logSuccess("LOGIN", "USER", user.getId(), null);
            log.info("User logged in successfully: {}", user.getUsername());
            
            return response;
            
        } catch (BadCredentialsException e) {
            auditService.logFailure("LOGIN", "USER", null, loginRequest.getUsername());
            throw e;
        }
    }
    
    @Transactional
    public void logout() {
        try {
            String token = tokenProvider.getTokenFromRequest(request);
            if (token != null) {
                String tokenHash = hashToken(token);
                sessionRepository.findByTokenHash(tokenHash).ifPresent(session -> {
                    session.setIsActive(false);
                    sessionRepository.save(session);
                    
                    auditService.logSuccess("LOGOUT", "USER", session.getUserId(), null);
                    log.info("User logged out successfully");
                });
            }
        } catch (Exception e) {
            log.error("Error during logout", e);
        }
    }
    
    @Transactional
    protected void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        user.setLastFailedLogin(LocalDateTime.now());
        
        if (attempts >= maxFailedAttempts) {
            user.setIsLocked(true);
            log.warn("Account locked due to too many failed login attempts: {}", user.getUsername());
            auditService.logWarning("ACCOUNT_LOCKED", "USER", user.getId(), 
                    "Account locked after " + attempts + " failed attempts");
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    protected void handleConcurrentSessions(Long userId) {
        long activeSessions = sessionRepository.countByUserIdAndIsActive(userId, true);
        
        if (activeSessions >= maxConcurrentSessions) {
            // Deactivate oldest sessions
            sessionRepository.findByUserIdAndIsActive(userId, true)
                    .stream()
                    .sorted((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                    .limit(activeSessions - maxConcurrentSessions + 1)
                    .forEach(session -> {
                        session.setIsActive(false);
                        sessionRepository.save(session);
                    });
        }
    }
    
    @Transactional
    protected void createSession(Long userId, String token) {
        String tokenHash = hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(jwtExpiration * 1_000_000);
        
        Session session = Session.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .ipAddress(getClientIp())
                .userAgent(request.getHeader("User-Agent"))
                .expiresAt(expiresAt)
                .build();
        
        sessionRepository.save(session);
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
    
    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
