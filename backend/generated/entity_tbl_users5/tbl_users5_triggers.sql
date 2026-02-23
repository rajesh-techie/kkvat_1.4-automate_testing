-- Trigger: update updated_at on tbl_users5
DELIMITER $$
CREATE TRIGGER trg_tbl_users5_upd BEFORE UPDATE ON tbl_users5 FOR EACH ROW BEGIN
  SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;
