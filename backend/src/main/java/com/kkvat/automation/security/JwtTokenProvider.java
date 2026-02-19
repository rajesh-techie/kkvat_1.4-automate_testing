package com.kkvat.automation.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${app.security.jwt.refresh-expiration}")
    private long refreshExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal.getUsername());
    }
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (SecurityException | MalformedJwtException ex) {
            logger.debug("Invalid JWT signature when parsing username: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.debug("Expired JWT token when parsing username: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.debug("Unsupported JWT token when parsing username: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.debug("JWT claims string is empty when parsing username: {}", ex.getMessage());
        } catch (JwtException ex) {
            logger.debug("JWT processing error when parsing username: {}", ex.getMessage());
        }
        return null;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            logger.debug("Invalid JWT signature: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.debug("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.debug("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.debug("JWT claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            logger.debug("JWT processing error: {}", ex.getMessage());
        }
        return false;
    }
    
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration();
    }
}
