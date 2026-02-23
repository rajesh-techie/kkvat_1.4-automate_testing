-- DDL: Create main entity table TBL_USERS (derived from payload)
CREATE TABLE IF NOT EXISTS `TBL_USERS` (
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
-- Columns generated from payload `columns[].column_name` and `column_datatype`/`column_length`.
-- Primary key: `userlogin` (column_primary=1 in payload).
-- Modify types/constraints if you prefer a surrogate `id` column instead.
