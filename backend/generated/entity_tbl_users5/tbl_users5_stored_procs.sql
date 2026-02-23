-- Stored procedure: insert into tbl_users5
DELIMITER $$
CREATE PROCEDURE sp_insert_tbl_users5(IN p_name VARCHAR(255))
BEGIN
  INSERT INTO tbl_users5(name) VALUES (p_name);
END$$
DELIMITER ;
