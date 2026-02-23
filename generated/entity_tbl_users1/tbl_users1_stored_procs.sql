-- Stored procedure: insert into tbl_users1
DELIMITER $$
CREATE PROCEDURE sp_insert_tbl_users1(IN p_name VARCHAR(255))
BEGIN
  INSERT INTO tbl_users1(name) VALUES (p_name);
END$$
DELIMITER ;
