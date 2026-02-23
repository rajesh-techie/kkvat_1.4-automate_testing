-- Create or replace views for generated tables
-- Views present all columns from the corresponding tables for read-only consumption

CREATE OR REPLACE VIEW `vw_tbl_users` AS
SELECT * FROM `TBL_USERS`;

CREATE OR REPLACE VIEW `vw_tbl_users_instance` AS
SELECT * FROM `TBL_USERS_INSTANCE`;

CREATE OR REPLACE VIEW `vw_tbl_users_audit` AS
SELECT * FROM `TBL_USERS_AUDIT`;

CREATE OR REPLACE VIEW `vw_tbl_users_archive` AS
SELECT * FROM `TBL_USERS_ARCHIVE`;

-- Notes:
-- - These are simple SELECT * views to expose the full row shape.
-- - Apply with: mysql -u root -padmin kkvat_automation < ./database/create_views.sql
-- - Consider granting SELECT on the views to roles/users as appropriate:
--   GRANT SELECT ON vw_tbl_users TO 'some_user'@'localhost';
