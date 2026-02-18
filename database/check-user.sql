USE kkvat_automation;
SELECT username, LEFT(password_hash, 30) as hash_start, is_active, is_locked, failed_login_attempts 
FROM users WHERE username='admin';
