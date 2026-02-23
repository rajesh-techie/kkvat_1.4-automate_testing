-- TBL_USERS_AUDIT (diff-only audit table)
CREATE TABLE IF NOT EXISTS `TBL_USERS_AUDIT` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `target_pk` VARCHAR(1024) NOT NULL,
  `action_type` VARCHAR(32) NOT NULL,
  `changed_columns` JSON NOT NULL,
  `old_values` JSON,
  `new_values` JSON NOT NULL,
  `changed_by` VARCHAR(64),
  `approved_by` VARCHAR(64),
  `approved_at` TIMESTAMP NULL,
  `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `comments` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Recommended indexes:
-- CREATE INDEX idx_users_audit_target_pk ON TBL_USERS_AUDIT(target_pk);
-- CREATE INDEX idx_users_audit_changed_at ON TBL_USERS_AUDIT(changed_at);
-- Notes:
-- - `target_pk` should match the format used in TBL_USERS_INSTANCE.target_pk (string or serialized JSON for composite PKs).
-- - `old_values` / `new_values` store only changed columns (diff). Example changed_columns: ["firstname","isActive"]
