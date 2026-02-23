-- Insert generated menu item for the entity and grant role permissions
-- This script uses values from your payload example:
--   parentMenu = 'Administration'
--   whichRoleIsEligible = 'ADMIN'
--   entityName/entityTableName = 'TBL_USERS'
-- It is idempotent (uses ON DUPLICATE KEY UPDATE pattern where appropriate).

-- 1) Insert menu item under the parent menu found by display_name
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order)
VALUES (
  'tbl_users', -- menu internal name (use entityTableName lowercased)
  'Users',     -- display name (human-friendly)
  'Generated menu for entity TBL_USERS',
  '/tbl-users',
  'table_chart',
  (SELECT id FROM menu_items WHERE display_name = 'Administration' LIMIT 1),
  999
)
ON DUPLICATE KEY UPDATE id = id;

-- 2) Grant access to the specified role for the newly inserted menu item
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), m.id
FROM menu_items m
WHERE m.name = 'tbl_users'
ON DUPLICATE KEY UPDATE id = id;

-- Notes:
-- - Adjust 'tbl_users' / 'Users' / '/tbl-users' as needed for other payload values.
-- - If parent menu is not found, the parent_menu_item_id will be NULL; create the parent first or change the SELECT.
-- - To support other roles, replace 'ADMIN' with the desired role name or add more INSERT statements.
