-- Stored procedure: move rows from main table to archive and clean up
-- (same as sp_move_to_archive_and_cleanup)
-- Parameters:
--  p_main_table: name of the main table (e.g., 'TBL_USERS')
--  p_archive_table: name of the archive table (e.g., 'TBL_USERS_ARCHIVE')
--  p_criteria_field: field name used in the criteria (e.g., 'isActive')
--  p_criteria_value: value to match (e.g., '0')
--  p_main_months: age in months in main table to qualify for archival (e.g., 6)
--  p_archive_months: additional months in archive before permanent deletion (e.g., 2)

DELIMITER $$
DROP PROCEDURE IF EXISTS `sp_move_to_archive_and_cleanup`$$
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

  -- Build simple equality condition. For safety, pass literal values appropriate for your column types.
  SET @cond := CONCAT(p_criteria_field, ' = ', QUOTE(p_criteria_value));

  -- 1) Insert old rows into archive
  SET @move_sql := CONCAT('INSERT INTO `', p_archive_table, '` SELECT * FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_move FROM @move_sql;
  EXECUTE stmt_move;
  DEALLOCATE PREPARE stmt_move;

  -- 2) Remove moved rows from main table
  SET @delete_sql := CONCAT('DELETE FROM `', p_main_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', p_main_months, ' MONTH)');
  PREPARE stmt_delete FROM @delete_sql;
  EXECUTE stmt_delete;
  DEALLOCATE PREPARE stmt_delete;

  -- 3) Clean up archive older than main_months + archive_months
  SET v_total_months := p_main_months + p_archive_months;
  SET @cleanup_sql := CONCAT('DELETE FROM `', p_archive_table, '` WHERE ', @cond, ' AND updated_at < (NOW() - INTERVAL ', v_total_months, ' MONTH)');
  PREPARE stmt_cleanup FROM @cleanup_sql;
  EXECUTE stmt_cleanup;
  DEALLOCATE PREPARE stmt_cleanup;

END$$
DELIMITER ;

-- Example usage:
-- CALL sp_move_to_archive_and_cleanup('TBL_USERS', 'TBL_USERS_ARCHIVE', 'isActive', '0', 6, 2);

-- Safety notes:
-- - Assumes identical table structures between main and archive.
-- - For composite/more complex criteria, extend the procedure to accept a full WHERE clause.
-- - For very large datasets, consider batching or a safer transactional approach.
