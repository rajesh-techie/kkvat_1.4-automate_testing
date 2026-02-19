-- Create physical SQL views for Reports (Users, Groups, Roles)
USE kkvat_automation;

-- Users view
DROP VIEW IF EXISTS report_view_users;
CREATE VIEW report_view_users AS
SELECT
  id,
  username,
  email,
  first_name AS firstName,
  last_name AS lastName,
  role,
  is_active AS isActive,
  created_at AS createdAt
FROM users;

-- Groups view
DROP VIEW IF EXISTS report_view_groups;
CREATE VIEW report_view_groups AS
SELECT
  id,
  name,
  description,
  is_active AS isActive,
  created_at AS createdAt
FROM `groups`;

-- Roles view
DROP VIEW IF EXISTS report_view_roles;
CREATE VIEW report_view_roles AS
SELECT
  id,
  name,
  description,
  is_active AS isActive,
  created_at AS createdAt
FROM roles;

-- Register these views in report_views table (idempotent inserts)
INSERT INTO report_views (name, display_name, description, table_name)
SELECT * FROM (SELECT 'users_view' AS name, 'Users' AS display_name, 'View of users table' AS description, 'report_view_users' AS table_name) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM report_views rv WHERE rv.name = 'users_view');

INSERT INTO report_views (name, display_name, description, table_name)
SELECT * FROM (SELECT 'groups_view' AS name, 'Groups' AS display_name, 'View of groups table' AS description, 'report_view_groups' AS table_name) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM report_views rv WHERE rv.name = 'groups_view');

INSERT INTO report_views (name, display_name, description, table_name)
SELECT * FROM (SELECT 'roles_view' AS name, 'Roles' AS display_name, 'View of roles table' AS description, 'report_view_roles' AS table_name) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM report_views rv WHERE rv.name = 'roles_view');

-- Populate report_view_fields for users_view
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'username', 'Username', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'email', 'Email', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'firstName', 'First Name', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'lastName', 'Last Name', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'role', 'Role', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'isActive', 'Active', 'BOOLEAN', 1, 1 FROM report_views rv WHERE rv.name = 'users_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

-- Populate report_view_fields for groups_view
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'name', 'Name', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'groups_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'description', 'Description', 'TEXT', 0, 0 FROM report_views rv WHERE rv.name = 'groups_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'isActive', 'Active', 'BOOLEAN', 1, 1 FROM report_views rv WHERE rv.name = 'groups_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

-- Populate report_view_fields for roles_view
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'name', 'Name', 'VARCHAR', 1, 1 FROM report_views rv WHERE rv.name = 'roles_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'description', 'Description', 'TEXT', 0, 0 FROM report_views rv WHERE rv.name = 'roles_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable)
SELECT rv.id, 'isActive', 'Active', 'BOOLEAN', 1, 1 FROM report_views rv WHERE rv.name = 'roles_view'
ON DUPLICATE KEY UPDATE field_name = field_name;

-- End of script
