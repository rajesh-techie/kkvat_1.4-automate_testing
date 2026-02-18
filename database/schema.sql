-- KKVat Automation Platform Database Schema
-- MySQL 8.x
-- Compliant with NIST 800-53 and CIS Benchmarks

-- Create database
CREATE DATABASE IF NOT EXISTS kkvat_automation 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE kkvat_automation;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_failed_login TIMESTAMP NULL,
    password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_expires_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- Groups table
CREATE TABLE IF NOT EXISTS `groups` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- User-Group mapping table
CREATE TABLE IF NOT EXISTS user_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),
    UNIQUE KEY unique_user_group (user_id, group_id),
    INDEX idx_user_id (user_id),
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB;

-- Test Cases table
CREATE TABLE IF NOT EXISTS test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    recorded_actions JSON NOT NULL,
    status ENUM('DRAFT', 'ACTIVE', 'DEPRECATED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    group_id BIGINT NULL,
    tags VARCHAR(500),
    base_url VARCHAR(500),
    timeout_seconds INT NOT NULL DEFAULT 30,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_group_id (group_id),
    INDEX idx_created_by (created_by),
    FULLTEXT idx_description (description)
) ENGINE=InnoDB;

-- Test Executions table
CREATE TABLE IF NOT EXISTS test_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    status ENUM('RUNNING', 'PASSED', 'FAILED', 'SKIPPED', 'ERROR') NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration_ms BIGINT NULL,
    result_json JSON,
    error_message TEXT,
    screenshots_path VARCHAR(500),
    video_path VARCHAR(500),
    executed_by BIGINT NOT NULL,
    browser VARCHAR(50),
    environment VARCHAR(50),
    FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    FOREIGN KEY (executed_by) REFERENCES users(id),
    INDEX idx_test_case_id (test_case_id),
    INDEX idx_status (status),
    INDEX idx_executed_by (executed_by),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB;

-- Sessions table (for JWT token tracking)
CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_accessed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_token_hash (token_hash),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- Recording Sessions table (for per-session recording storage)
CREATE TABLE IF NOT EXISTS recording_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_url VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    recorded_actions JSON,
    action_count INT,
    created_by BIGINT NOT NULL,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    group_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE SET NULL,
    INDEX idx_session_id (session_id),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB;

-- Audit Logs table (NIST compliance requirement)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT NULL,
    details JSON,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    status ENUM('SUCCESS', 'FAILURE', 'WARNING') NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_status (status),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB;

-- Password History table (to prevent password reuse - NIST requirement)
CREATE TABLE IF NOT EXISTS password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- Available Views/Data Sources for Report Builder
CREATE TABLE IF NOT EXISTS report_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    table_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- View Fields/Columns available in each View
CREATE TABLE IF NOT EXISTS report_view_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    view_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    is_sortable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (view_id) REFERENCES report_views(id) ON DELETE CASCADE,
    UNIQUE KEY unique_view_field (view_id, field_name),
    INDEX idx_view_id (view_id)
) ENGINE=InnoDB;

-- Report Templates (CRUD operations)
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    view_id BIGINT NOT NULL,
    selected_columns JSON NOT NULL,
    filter_conditions JSON,
    sort_config JSON,
    report_type ENUM('EXECUTION', 'USER_ACTIVITY', 'CUSTOM') NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (view_id) REFERENCES report_views(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_report_type (report_type),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB;

-- Report Schedules (Schedule report for recurring generation)
CREATE TABLE IF NOT EXISTS report_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    schedule_name VARCHAR(200) NOT NULL,
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY') NOT NULL,
    day_of_week INT NULL,
    day_of_month INT NULL,
    time_of_day TIME NOT NULL,
    email_recipients VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_executed TIMESTAMP NULL,
    next_execution TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_report_id (report_id),
    INDEX idx_is_active (is_active),
    INDEX idx_next_execution (next_execution)
) ENGINE=InnoDB;

-- Report Executions (Download reports history)
CREATE TABLE IF NOT EXISTS report_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    execution_type ENUM('MANUAL', 'SCHEDULED', 'API') NOT NULL,
    status ENUM('PENDING', 'GENERATING', 'COMPLETED', 'FAILED') NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration_ms BIGINT NULL,
    file_path VARCHAR(500),
    file_size BIGINT NULL,
    row_count INT NULL,
    error_message TEXT,
    executed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES report_schedules(id) ON DELETE SET NULL,
    FOREIGN KEY (executed_by) REFERENCES users(id),
    INDEX idx_report_id (report_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_executed_by (executed_by)
) ENGINE=InnoDB;

-- Roles table (for role-based access control)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- User-Role mapping table (replaces enum-based roles)
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),
    UNIQUE KEY unique_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB;

-- Group-Role mapping table
CREATE TABLE IF NOT EXISTS group_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES `groups`(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),
    UNIQUE KEY unique_group_role (group_id, role_id),
    INDEX idx_group_id (group_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB;

-- Menu Items table (hierarchical navigation structure)
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    route_link VARCHAR(500),
    icon_name VARCHAR(100),
    parent_menu_item_id BIGINT NULL,
    menu_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    INDEX idx_name (name),
    INDEX idx_parent_menu_item_id (parent_menu_item_id),
    INDEX idx_menu_order (menu_order),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- Role-Menu Item mapping table (access control)
CREATE TABLE IF NOT EXISTS role_menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    UNIQUE KEY unique_role_menu_item (role_id, menu_item_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_item_id (menu_item_id)
) ENGINE=InnoDB;

-- Insert default Report Views (for test executions and user activity)
INSERT INTO report_views (name, display_name, description, table_name) VALUES
('test_executions_view', 'Test Executions', 'View of all test execution records', 'test_executions'),
('user_activity_view', 'User Activity', 'View of all user activities from audit logs', 'audit_logs'),
('test_cases_view', 'Test Cases', 'View of all test cases', 'test_cases');

-- Insert default admin user (password: Admin@123456)
-- Password hashed with BCrypt (strength 12)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, password_changed_at, password_expires_at)
VALUES (
    'admin',
    'admin@kkvat.local',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYVYdTBgLOe',
    'System',
    'Administrator',
    'ADMIN',
    CURRENT_TIMESTAMP,
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 90 DAY)
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full system access'),
('TEST_MANAGER', 'Can manage test cases and execution'),
('TESTER', 'Can execute tests and view results'),
('VIEWER', 'Read-only access to reports and test results');

-- Assign admin user to ADMIN role (admin user id = 1)
INSERT INTO user_roles (user_id, role_id, assigned_by) 
SELECT 1, id, 1 FROM roles WHERE name = 'ADMIN';

-- Insert default menu items (hierarchical structure)
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order) VALUES
-- Main menus
('dashboard', 'Dashboard', 'Dashboard', '/dashboard', 'dashboard', NULL, 1),
('test-management', 'Test Management', 'Manage test cases and executions', '/test-management', 'test_cases', NULL, 2),
('reports', 'Reports', 'View and configure reports', '/reports', 'assessment', NULL, 3),
('administration', 'Administration', 'System administration and settings', NULL, 'settings', NULL, 4);

-- Insert submenu items
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order) VALUES
-- Test Management Submenu (parent_id = 2)
('test-cases', 'Test Cases', 'Create and manage test cases', '/test-cases', 'description', 2, 1),
('test-execution', 'Test Execution', 'Execute and monitor tests', '/test-execution', 'play_circle_outline', 2, 2),
('execution-history', 'Execution History', 'View execution history and logs', '/execution-history', 'history', 2, 3),
-- Reports Submenu (parent_id = 3)
('test-reports', 'Test Reports', 'View test execution reports', '/reports/test', 'bar_chart', 3, 1),
('user-reports', 'User Reports', 'View user activity reports', '/reports/user', 'people', 3, 2),
('report-builder', 'Report Builder', 'Create custom reports', '/report-builder', 'edit', 3, 3),
-- Administration Submenu (parent_id = 4)
('users', 'Users', 'Manage users and permissions', '/admin/users', 'person', 4, 1),
('groups', 'Groups', 'Manage user groups', '/admin/groups', 'people', 4, 2),
('roles', 'Roles', 'Manage roles and permissions', '/admin/roles', 'security', 4, 3),
('settings', 'Settings', 'System settings and configuration', '/admin/settings', 'tune', 4, 4);

-- Assign menu items to ADMIN role (all menus)
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT r.id, m.id FROM roles r, menu_items m 
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE id=id;

-- Assign menu items to TEST_MANAGER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT r.id, m.id FROM roles r, menu_items m 
WHERE r.name = 'TEST_MANAGER' AND m.name IN (
  'dashboard', 'test-management', 'test-cases', 'test-execution', 
  'execution-history', 'reports', 'test-reports', 'user-reports', 'report-builder'
)
ON DUPLICATE KEY UPDATE id=id;

-- Assign menu items to TESTER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT r.id, m.id FROM roles r, menu_items m 
WHERE r.name = 'TESTER' AND m.name IN (
  'dashboard', 'test-management', 'test-execution', 'execution-history', 
  'reports', 'test-reports', 'user-reports'
)
ON DUPLICATE KEY UPDATE id=id;

-- Assign menu items to VIEWER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT r.id, m.id FROM roles r, menu_items m 
WHERE r.name = 'VIEWER' AND m.name IN (
  'dashboard', 'reports', 'test-reports', 'user-reports'
)
ON DUPLICATE KEY UPDATE id=id;

-- Create database user with limited privileges (principle of least privilege)
-- Run these commands as MySQL root user:
-- CREATE USER 'kkvat_user'@'localhost' IDENTIFIED BY 'YourSecurePassword@123';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON kkvat_automation.* TO 'kkvat_user'@'localhost';
-- FLUSH PRIVILEGES;

