-- Trigger: update updated_at on tbl_users1
DELIMITER $$
CREATE TRIGGER trg_tbl_users1_upd BEFORE UPDATE ON tbl_users1 FOR EACH ROW BEGIN
  SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;
