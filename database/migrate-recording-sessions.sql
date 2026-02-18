-- Add missing columns to recording_sessions table
USE kkvat_automation;

-- Add name column (if not exists - check using information_schema)
SET @dbname = DATABASE();
SET @tablename = 'recording_sessions';
SET @columnname = 'name';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE 
    (TABLE_NAME = @tablename) AND (TABLE_SCHEMA = @dbname) AND (COLUMN_NAME = @columnname)) > 0,
  "SELECT 1",
  "ALTER TABLE recording_sessions ADD COLUMN name VARCHAR(200) NOT NULL DEFAULT 'Untitled Recording' AFTER session_id"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add description column (if not exists)
SET @columnname = 'description';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE 
    (TABLE_NAME = @tablename) AND (TABLE_SCHEMA = @dbname) AND (COLUMN_NAME = @columnname)) > 0,
  "SELECT 1",
  "ALTER TABLE recording_sessions ADD COLUMN description TEXT AFTER name"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add base_url column (if not exists)
SET @columnname = 'base_url';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE 
    (TABLE_NAME = @tablename) AND (TABLE_SCHEMA = @dbname) AND (COLUMN_NAME = @columnname)) > 0,
  "SELECT 1",
  "ALTER TABLE recording_sessions ADD COLUMN base_url VARCHAR(500) NOT NULL DEFAULT 'http://localhost' AFTER description"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'Migration completed successfully' as Status;
