-- Detailed stored procedures for tbl_users5
DELIMITER $$
CREATE PROCEDURE sp_move_to_archive_tbl_users5(
  IN p_criteria_field VARCHAR(128),
  IN p_criteria_value VARCHAR(255),
  IN p_main_months INT,
  IN p_archive_months INT
)
BEGIN
  DECLARE v_move_sql TEXT;
  DECLARE v_delete_sql TEXT;
  DECLARE v_cleanup_sql TEXT;

  SET @cond := CONCAT(p_criteria_field, ' = ', QUOTE(p_criteria_value));

  SET @move_sql := CONCAT('INSERT INTO `', 'tbl_users5_archive', '` SELECT * FROM `', 'tbl_users5', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_move FROM @move_sql;
  EXECUTE stmt_move;
  DEALLOCATE PREPARE stmt_move;

  SET @delete_sql := CONCAT('DELETE FROM `', 'tbl_users5', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_delete FROM @delete_sql;
  EXECUTE stmt_delete;
  DEALLOCATE PREPARE stmt_delete;

  SET v_total_months := p_main_months + p_archive_months;
  SET @cleanup_sql := CONCAT('DELETE FROM `', 'tbl_users5_archive', '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', v_total_months, ' MONTH)');
  PREPARE stmt_cleanup FROM @cleanup_sql;
  EXECUTE stmt_cleanup;
  DEALLOCATE PREPARE stmt_cleanup;

END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE sp_move_to_archive_tbl_users5_now()
BEGIN
  CALL sp_move_to_archive_tbl_users5('isactive', '0', 2, 2);
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE sp_purge_archive_tbl_users5()
BEGIN
  DELETE FROM tbl_users5_archive WHERE created_at < DATE_SUB(CURRENT_DATE, INTERVAL 2 MONTH);
END$$
DELIMITER ;

-- Stored procedure: move rows from main table to archive and clean up archive
-- Parameters: p_main_table, p_archive_table, p_criteria_field, p_criteria_value, p_main_months, p_archive_months
DELIMITER $$
CREATE DEFINER=CURRENT_USER PROCEDURE `sp_move_to_archive_and_cleanup`(
  IN p_main_table VARCHAR(128),
  IN p_archive_table VARCHAR(128),
  IN p_criteria_field VARCHAR(128),
  IN p_criteria_value VARCHAR(255),
  IN p_main_months INT,
  IN p_archive_months INT
)
BEGIN
  DECLARE v_move_sql TEXT;
  DECLARE v_delete_sql TEXT;
  DECLARE v_cleanup_sql TEXT;
  DECLARE v_total_months INT;

  SET @cond := CONCAT(p_criteria_field, ' = ', QUOTE(p_criteria_value));

  SET @move_sql := CONCAT('INSERT INTO `', p_archive_table, '` SELECT * FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_move FROM @move_sql;
  EXECUTE stmt_move;
  DEALLOCATE PREPARE stmt_move;

  SET @delete_sql := CONCAT('DELETE FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_delete FROM @delete_sql;
  EXECUTE stmt_delete;
  DEALLOCATE PREPARE stmt_delete;

  SET v_total_months := p_main_months + p_archive_months;
  SET @cleanup_sql := CONCAT('DELETE FROM `', p_archive_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', v_total_months, ' MONTH)');
  PREPARE stmt_cleanup FROM @cleanup_sql;
  EXECUTE stmt_cleanup;
  DEALLOCATE PREPARE stmt_cleanup;

END$$
DELIMITER ;

-- Example: CALL sp_move_to_archive_and_cleanup('tbl_users5', 'tbl_users5_archive', 'isActive', '0', 6, 2);
