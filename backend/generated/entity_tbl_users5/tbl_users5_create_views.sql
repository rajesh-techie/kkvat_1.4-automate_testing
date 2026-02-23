-- Create or replace views for generated tables
-- Views present all columns from the corresponding tables for read-only consumption

CREATE OR REPLACE VIEW `vw_tbl_users5` AS
SELECT * FROM `tbl_users5`;

CREATE OR REPLACE VIEW `vw_tbl_users5_instance` AS
SELECT * FROM `tbl_users5_instance`;

CREATE OR REPLACE VIEW `vw_tbl_users5_audit` AS
SELECT * FROM `tbl_users5_audit`;

CREATE OR REPLACE VIEW `vw_tbl_users5_archive` AS
SELECT * FROM `tbl_users5_archive`;

-- Notes:
-- - These are simple SELECT * views to expose the full row shape.
-- - Apply with: mysql -u root -padmin kkvat_automation < ./database/create_views.sql
-- - Consider granting SELECT on the views to roles/users as appropriate.
