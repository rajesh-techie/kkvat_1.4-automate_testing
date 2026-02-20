-- Insert Entity Management menu item under Administration (parent id = 4)
INSERT INTO menu_items (name, display_name, description, route_link, icon_name, parent_menu_item_id, menu_order)
VALUES
('entity-management', 'Entity Management', 'Manage dynamic entity generator configurations', '/entity-management', 'admin_panel_settings', 4, 5)
ON DUPLICATE KEY UPDATE id=id;

-- Grant access to ADMIN role for Entity Management
INSERT INTO role_menu_items (role_id, menu_item_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), m.id FROM menu_items m WHERE m.name = 'entity-management'
ON DUPLICATE KEY UPDATE id=id;
