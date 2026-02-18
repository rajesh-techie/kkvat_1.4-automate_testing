USE kkvat_automation;

-- Reset admin password to Admin@123456
UPDATE users 
SET password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVYdTBgLOe',
    password_changed_at = CURRENT_TIMESTAMP,
    password_expires_at = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 90 DAY),
    failed_login_attempts = 0,
    is_locked = 0,
    last_failed_login = NULL
WHERE username = 'admin';

SELECT 'Admin password reset successfully' as Status;
SELECT id, username, email, role, is_active, is_locked FROM users WHERE username='admin';
