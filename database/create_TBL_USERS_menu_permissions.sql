-- Insert menu item and grant permission for generated entity TBL_USERS
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order)
VALUES
('tbl_users', 'Users', 'Generated menu for entity TBL_USERS', '/tbl-users', 'table_chart', (SELECT id FROM menu_items WHERE display_name = 'Administration' LIMIT 1), 999)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), m.id FROM menu_items m WHERE m.name = 'tbl_users'
ON DUPLICATE KEY UPDATE id = id;

-- Notes: replace 'ADMIN' with payload `whichRoleIsEligible` if different.
