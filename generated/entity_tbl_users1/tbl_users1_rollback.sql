-- Rollback: drop generated table and stored procs for tbl_users1
DROP PROCEDURE IF EXISTS sp_insert_tbl_users1;
DROP TRIGGER IF EXISTS trg_tbl_users1_upd;
DROP TABLE IF EXISTS tbl_users1;
