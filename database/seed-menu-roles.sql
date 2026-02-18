USE kkvat_automation;

-- Clear existing menu data (if any)
DELETE FROM role_menu_items;
DELETE FROM menu_items;
DELETE FROM user_roles;
DELETE FROM roles;

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full system access'),
('TEST_MANAGER', 'Can manage test cases and execution'),
('TESTER', 'Can execute tests and view results'),
('VIEWER', 'Read-only access to reports and test results');

-- Insert default menu items (hierarchical structure)
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order) VALUES
-- Main menus (no parent)
('dashboard', 'Dashboard', 'Dashboard', '/dashboard', 'dashboard', NULL, 1),
('test-management', 'Test Management', 'Manage test cases and executions', '/test-management', 'test_cases', NULL, 2),
('reports', 'Reports', 'View and configure reports', '/reports', 'assessment', NULL, 3),
('administration', 'Administration', 'System administration and settings', NULL, 'settings', NULL, 4);

-- Insert submenu items (these will have parent_menu_item_id set)
-- First, get the IDs of parent items and insert children
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order) VALUES
-- Test Management Submenu (parent_id will be 2 after first 4 inserts)
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

-- Assign admin user to ADMIN role (user_id = 1)
INSERT INTO user_roles (user_id, role_id, assigned_by) 
VALUES (1, (SELECT id FROM roles WHERE name = 'ADMIN'), 1);

-- Assign menu items to ADMIN role (all items)
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), id FROM menu_items;

-- Assign menu items to TEST_MANAGER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'TEST_MANAGER'), id FROM menu_items
WHERE name IN (
  'dashboard', 'test-management', 'test-cases', 'test-execution', 
  'execution-history', 'reports', 'test-reports', 'user-reports', 'report-builder'
);

-- Assign menu items to TESTER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'TESTER'), id FROM menu_items
WHERE name IN (
  'dashboard', 'test-management', 'test-execution', 'execution-history', 
  'reports', 'test-reports', 'user-reports'
);

-- Assign menu items to VIEWER role
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'VIEWER'), id FROM menu_items
WHERE name IN (
  'dashboard', 'reports', 'test-reports', 'user-reports'
);
