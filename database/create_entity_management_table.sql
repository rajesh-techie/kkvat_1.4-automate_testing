-- Creates the entity_management table used by the Entity Generator UI
-- Run this script against the application database (MySQL)

CREATE TABLE IF NOT EXISTS `entity_management` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_by` BIGINT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  `entity_name` VARCHAR(255) NOT NULL,
  `entity_table_name` VARCHAR(255) NOT NULL,

  `entity_columns_count` INT DEFAULT NULL,
  `entity_column_next` VARCHAR(255) DEFAULT NULL,

  -- Per-column properties (these are examples that apply to the current/active column definition)
  `is_column_dropdown` TINYINT(1) DEFAULT 0,
  `is_column_checkbox` TINYINT(1) DEFAULT 0,
  `is_column_radio` TINYINT(1) DEFAULT 0,
  `is_column_blob` TINYINT(1) DEFAULT 0,
  `column_type` VARCHAR(100) DEFAULT NULL,
  `column_length` INT DEFAULT NULL,
  `column_primary` TINYINT(1) DEFAULT 0,
  `column_index` TINYINT(1) DEFAULT 0,
  `column_part_of_search` TINYINT(1) DEFAULT 0,
  `is_referential_integrity` TINYINT(1) DEFAULT 0,

  `do_we_need_workflow` TINYINT(1) DEFAULT 0,
  `do_we_need_2_level_workflow` TINYINT(1) DEFAULT 0,
  `do_we_need_1_level_workflow` TINYINT(1) DEFAULT 0,
  `workflow_status` VARCHAR(100) DEFAULT NULL,

  `do_we_need_audit_table` TINYINT(1) DEFAULT 0,
  `do_we_need_archive_records` TINYINT(1) DEFAULT 0,

  -- Criteria fields/values stored as JSON text
  `criteria_fields` JSON DEFAULT NULL,
  `criteria_values` JSON DEFAULT NULL,

  `do_we_need_create_view` TINYINT(1) DEFAULT 0,
  `how_many_months_main_table` INT DEFAULT NULL,
  `how_many_months_archive_table` INT DEFAULT NULL,
  `criteria_to_move_from_main_to_archive_table` TEXT DEFAULT NULL,
  `criteria_to_move_from_archive_to_delete_table` TEXT DEFAULT NULL,

  `things_to_create` TEXT DEFAULT NULL,
  `parent_menu` VARCHAR(255) DEFAULT NULL,
  `which_role_is_eligible` VARCHAR(255) DEFAULT NULL,
  `columns` JSON DEFAULT NULL,

  `status` VARCHAR(50) DEFAULT 'PENDING',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional detailed per-entity column definitions table
CREATE TABLE IF NOT EXISTS `entity_management_columns` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `entity_management_id` BIGINT NOT NULL,
  `column_seq` INT NOT NULL DEFAULT 1,
  `column_name` VARCHAR(255) DEFAULT NULL,
  `column_length` INT DEFAULT NULL,
  `column_datatype` VARCHAR(50) DEFAULT NULL,
  `is_dropdown` TINYINT(1) DEFAULT 0,
  `is_radiobutton` TINYINT(1) DEFAULT 0,
  `is_checkbox` TINYINT(1) DEFAULT 0,
  `is_freefield` TINYINT(1) DEFAULT 0,
  `column_index` TINYINT(1) DEFAULT 0,
  `column_primary` TINYINT(1) DEFAULT 0,
  `column_part_of_search` TINYINT(1) DEFAULT 0,
  `column_referential_integrity` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_em_columns_entity` FOREIGN KEY (`entity_management_id`) REFERENCES `entity_management`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_entity_seq` (`entity_management_id`, `column_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$
DROP TRIGGER IF EXISTS `tr_em_columns_seq`$$
CREATE TRIGGER `tr_em_columns_seq`
BEFORE INSERT ON `entity_management_columns`
FOR EACH ROW
BEGIN
  IF NEW.column_seq IS NULL OR NEW.column_seq = 0 THEN
    SELECT COALESCE(MAX(column_seq), 0) + 1 INTO @nextSeq FROM entity_management_columns WHERE entity_management_id = NEW.entity_management_id;
    SET NEW.column_seq = @nextSeq;
  END IF;
END$$
DELIMITER ;
