-- TBL_USERS_INSTANCE (diff-only instance table)
CREATE TABLE IF NOT EXISTS `TBL_USERS_INSTANCE` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `action_type` VARCHAR(32) NOT NULL,
  `target_pk` VARCHAR(1024),
  `old_values` JSON,
  `new_values` JSON NOT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  `created_by` VARCHAR(64),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `approved_by` VARCHAR(64),
  `approved_at` TIMESTAMP NULL,
  `comments` TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Recommended indexes (create as needed):
-- CREATE INDEX idx_users_instance_status ON TBL_USERS_INSTANCE(status);
-- CREATE INDEX idx_users_instance_created_at ON TBL_USERS_INSTANCE(created_at);
-- Notes:
-- - `target_pk` stores the PK of the target row (string or serialized JSON for composite PKs).
-- - `old_values` and `new_values` should contain only changed columns (diff). For this entity the PK is `userlogin`.
