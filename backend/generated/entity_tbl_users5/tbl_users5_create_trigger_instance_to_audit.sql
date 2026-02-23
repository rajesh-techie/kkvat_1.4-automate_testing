-- Trigger to copy approved instance rows into TBL_USERS5_AUDIT
-- Creates triggers to insert audit rows when an instance is approved

DELIMITER $$

DROP TRIGGER IF EXISTS `trg_users5_instance_after_insert`$$
CREATE TRIGGER `trg_users5_instance_after_insert`
AFTER INSERT ON `TBL_USERS5_INSTANCE`
FOR EACH ROW
BEGIN
  IF NEW.status LIKE 'APPROVED%' THEN
    INSERT INTO `TBL_USERS5_AUDIT` (
      `target_pk`, `action_type`, `changed_columns`, `old_values`, `new_values`,
      `changed_by`, `approved_by`, `approved_at`, `changed_at`, `comments`
    ) VALUES (
      NEW.target_pk,
      NEW.action_type,
      JSON_KEYS(IFNULL(NEW.new_values, NEW.old_values)),
      NEW.old_values,
      NEW.new_values,
      NEW.created_by,
      NEW.approved_by,
      NEW.approved_at,
      NOW(),
      NEW.comments
    );
  END IF;
END$$

DROP TRIGGER IF EXISTS `trg_users5_instance_after_update`$$
CREATE TRIGGER `trg_users5_instance_after_update`
AFTER UPDATE ON `TBL_USERS5_INSTANCE`
FOR EACH ROW
BEGIN
  IF NEW.status LIKE 'APPROVED%' AND (OLD.status NOT LIKE 'APPROVED%') THEN
    INSERT INTO `TBL_USERS5_AUDIT` (
      `target_pk`, `action_type`, `changed_columns`, `old_values`, `new_values`,
      `changed_by`, `approved_by`, `approved_at`, `changed_at`, `comments`
    ) VALUES (
      NEW.target_pk,
      NEW.action_type,
      JSON_KEYS(IFNULL(NEW.new_values, NEW.old_values)),
      NEW.old_values,
      NEW.new_values,
      NEW.created_by,
      NEW.approved_by,
      NEW.approved_at,
      NOW(),
      NEW.comments
    );
  END IF;
END$$

DELIMITER ;

-- Notes:
-- - Triggers insert audit rows when an instance is created or updated into an APPROVED state.
-- - `changed_columns` is derived via JSON_KEYS on `new_values` (falls back to `old_values`).
-- - For composite or complex workflows, adjust guards to avoid duplicates.
