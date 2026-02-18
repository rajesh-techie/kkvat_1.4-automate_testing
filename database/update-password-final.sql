-- Update admin password with a verified BCrypt hash
-- Password: Admin@123456
-- Generated with BCrypt strength 12

UPDATE users 
SET password_hash = '$2a$12$dXGJmMzLjzH3qF5yVGxmPeKkP3zB3h9lqP3yqZX7M3QqZBxGxP3yC',
    password_changed_at = CURRENT_TIMESTAMP,
    password_expires_at = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 90 DAY),
    failed_login_attempts = 0,
    is_locked = 0,
    last_failed_login = NULL
WHERE username = 'admin';

SELECT 'Password updated' as Status;
