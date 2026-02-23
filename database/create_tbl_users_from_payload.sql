-- Generated CREATE TABLE for TBL_USERS based on payload columns
-- Columns are derived from payload.column_name and payload.column_datatype/column_length

CREATE TABLE IF NOT EXISTS `TBL_USERS` (
  `user_firstname` VARCHAR(100) DEFAULT NULL,
  `user_lastname`  VARCHAR(100) DEFAULT NULL,
  `user_login`     VARCHAR(100) NOT NULL,
  `isActive`       TINYINT(1) NOT NULL DEFAULT 1,
  `created_by` VARCHAR(64),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(64),
  `updated_at` TIMESTAMP NULL,
  PRIMARY KEY (`user_login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notes:
-- - Columns generated: `user_firstname`, `user_lastname`, `user_login` (PK), `isActive`.
-- - Type mapping used: `string` -> `VARCHAR(length)`, `number` with length=1 -> `TINYINT(1)`.
-- - `username` and `email` are intentionally omitted per requirements.
-- - If you prefer a surrogate `id` auto-increment primary key instead of `user_login`, regenerate with that option.
