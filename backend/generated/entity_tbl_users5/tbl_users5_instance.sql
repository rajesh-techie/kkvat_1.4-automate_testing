-- Instance table for tbl_users5 (workflow/approval queue)
CREATE TABLE IF NOT EXISTS `tbl_users5_instance` (
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
-- CREATE INDEX idx_tbl_users5_instance_status ON tbl_users5_instance(status);
-- CREATE INDEX idx_tbl_users5_instance_created_at ON tbl_users5_instance(created_at);
