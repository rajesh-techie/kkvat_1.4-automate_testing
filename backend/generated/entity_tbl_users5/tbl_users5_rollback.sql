-- Rollback: drop generated table and stored procs for tbl_users5
DROP PROCEDURE IF EXISTS sp_insert_tbl_users5;
DROP TRIGGER IF EXISTS trg_tbl_users5_upd;
DROP TABLE IF EXISTS tbl_users5;
