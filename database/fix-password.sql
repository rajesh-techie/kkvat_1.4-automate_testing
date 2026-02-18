-- Update admin password to "password" with BCrypt strength 12
-- This hash is for the password "password" with BCrypt strength 12
UPDATE users 
SET password_hash = '$2a$12$LkHmuCGX/6/P.Q7B6xOXNuCqNa3yE8.Pz3YK8a3qjN7SXjPZcxnGq',
    password_changed_at = CURRENT_TIMESTAMP,
    password_expires_at = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 90 DAY),
    failed_login_attempts = 0,
    is_locked = 0,
    last_failed_login = NULL
WHERE username = 'admin';

SELECT 'Admin password updated to: password' as Status;
