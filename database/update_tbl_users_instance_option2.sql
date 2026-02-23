-- Update/create TBL_USERS_INSTANCE to use diff-only audit format (option 2)
-- Database: kkvat_automation

CREATE TABLE IF NOT EXISTS TBL_USERS_INSTANCE (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  generated_table_name VARCHAR(128) NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  target_pk VARCHAR(255),
  old_values JSON,
  new_values JSON NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_by VARCHAR(64),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  approved_by VARCHAR(64),
  approved_at TIMESTAMP NULL,
  comments TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Remove legacy/full-snapshot/approval-stage columns if present
ALTER TABLE TBL_USERS_INSTANCE
  DROP COLUMN IF EXISTS current_stage,
  DROP COLUMN IF EXISTS approval_chain,
  DROP COLUMN IF EXISTS full_snapshot;

-- Ensure new columns exist (safe for existing tables)
ALTER TABLE TBL_USERS_INSTANCE
  ADD COLUMN IF NOT EXISTS generated_table_name VARCHAR(128) NOT NULL AFTER id,
  ADD COLUMN IF NOT EXISTS action_type VARCHAR(32) NOT NULL AFTER generated_table_name,
  ADD COLUMN IF NOT EXISTS target_pk VARCHAR(255) AFTER action_type,
  ADD COLUMN IF NOT EXISTS old_values JSON AFTER target_pk,
  ADD COLUMN IF NOT EXISTS new_values JSON AFTER old_values,
  ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDING' AFTER new_values,
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(64) AFTER status,
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER created_by,
  ADD COLUMN IF NOT EXISTS approved_by VARCHAR(64) AFTER created_at,
  ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP NULL AFTER approved_by,
  ADD COLUMN IF NOT EXISTS comments TEXT AFTER approved_at;

-- Recommended indexes (create manually as needed)
-- ALTER TABLE TBL_USERS_INSTANCE ADD INDEX idx_tbl_users_instance_status (status);
-- ALTER TABLE TBL_USERS_INSTANCE ADD INDEX idx_tbl_users_instance_created_at (created_at);

-- Notes:
--  - `old_values` / `new_values` should contain only the changed columns (diff), e.g. {"first_name":"Old"} / {"first_name":"New"}.
--  - `action_type` can be 'INSERT','UPDATE','DELETE' (or similar values your app uses).
--  - If you prefer explicit ENUMs or additional constraints, update the DDL accordingly.

-- End of file
