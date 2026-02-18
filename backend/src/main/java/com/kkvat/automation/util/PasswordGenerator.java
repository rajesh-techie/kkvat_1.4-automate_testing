package com.kkvat.automation.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordGenerator <password>");
            System.exit(1);
        }
        
        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash (strength 12): " + hash);
        System.out.println("\nSQL Update Statement:");
        System.out.println("UPDATE users SET password_hash = '" + hash + "', failed_login_attempts = 0, is_locked = 0 WHERE username = 'admin';");
        
        // Test the hash
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + (matches ? "PASS" : "FAIL"));
    }
}
