-- Add missing columns to recording_sessions table
USE kkvat_automation;

-- Add name column
ALTER TABLE recording_sessions 
ADD COLUMN name VARCHAR(200) NOT NULL DEFAULT 'Untitled Recording' AFTER session_id;

-- Add description column
ALTER TABLE recording_sessions
ADD COLUMN description TEXT AFTER name;

-- Add base_url column
ALTER TABLE recording_sessions
ADD COLUMN base_url VARCHAR(500) NOT NULL DEFAULT 'http://localhost' AFTER description;

-- Make start_time nullable (it was NOT NULL in old schema)
ALTER TABLE recording_sessions
MODIFY COLUMN start_time TIMESTAMP NULL;

SELECT 'Migration completed successfully' as Status;
