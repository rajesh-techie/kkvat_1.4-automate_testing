-- Trigger: copy approved instance rows into TBL_USERS_AUDIT
-- Creates two triggers: one for INSERT (if created already approved) and one for UPDATE (when status transitions to approved)

DELIMITER $$

DROP TRIGGER IF EXISTS `trg_users_instance_after_insert`$$
CREATE TRIGGER `trg_users_instance_after_insert`
AFTER INSERT ON `TBL_USERS_INSTANCE`
FOR EACH ROW
BEGIN
  IF NEW.status LIKE 'APPROVED%' THEN
    INSERT INTO `TBL_USERS_AUDIT` (
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

DROP TRIGGER IF EXISTS `trg_users_instance_after_update`$$
CREATE TRIGGER `trg_users_instance_after_update`
AFTER UPDATE ON `TBL_USERS_INSTANCE`
FOR EACH ROW
BEGIN
  -- Only fire when status transitions into an approved state
  IF NEW.status LIKE 'APPROVED%' AND (OLD.status NOT LIKE 'APPROVED%') THEN
    INSERT INTO `TBL_USERS_AUDIT` (
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
-- - The triggers use a simple pattern: when an instance becomes approved (status LIKE 'APPROVED%'), a corresponding audit row is inserted.
-- - `changed_columns` is derived from JSON_KEYS of `new_values` (falls back to `old_values` if new_values is NULL).
-- - For composite/complex approval flows, you may want to include additional guards to avoid duplicate audit rows.
