-- TBL_USERS_ARCHIVE (same structure as TBL_USERS)
-- This archive table mirrors the main TBL_USERS structure exactly.

CREATE TABLE IF NOT EXISTS `TBL_USERS_ARCHIVE` (
  `firstname` VARCHAR(100) DEFAULT NULL,
  `lastname` VARCHAR(100) DEFAULT NULL,
  `userlogin` VARCHAR(100) NOT NULL,
  `isActive` TINYINT(1) NOT NULL DEFAULT 1,
  `created_by` VARCHAR(64),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(64),
  `updated_at` TIMESTAMP NULL,
  PRIMARY KEY (`userlogin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notes:
-- - The archive table column set is identical to `TBL_USERS` so rows can be moved back/forth without transformation.
-- - If you prefer additional archive metadata, create a separate metadata table or extend the generator to include it.
