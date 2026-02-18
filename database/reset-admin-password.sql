-- Reset admin password
-- Password: admin (super simple for testing)
-- BCrypt hash with strength 10: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG

UPDATE users 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
    password_changed_at = CURRENT_TIMESTAMP,
    password_expires_at = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 90 DAY),
    failed_login_attempts = 0,
    is_locked = false,
    last_failed_login = NULL
WHERE username = 'admin';
