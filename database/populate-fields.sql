-- Populate report_view_fields with column definitions for each view
USE kkvat_automation;

-- Fields for test_executions_view
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable) VALUES
(1, 'id', 'Execution ID', 'BIGINT', 1, 1),
(1, 'test_case_id', 'Test Case', 'BIGINT', 1, 1),
(1, 'status', 'Status', 'VARCHAR', 1, 1),
(1, 'start_time', 'Start Time', 'TIMESTAMP', 1, 1),
(1, 'end_time', 'End Time', 'TIMESTAMP', 1, 1),
(1, 'duration_ms', 'Duration (ms)', 'BIGINT', 1, 1),
(1, 'error_message', 'Error Message', 'TEXT', 0, 0),
(1, 'executed_by', 'Executed By', 'BIGINT', 1, 1),
(1, 'created_at', 'Created At', 'TIMESTAMP', 1, 1);

-- Fields for user_activity_view  
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable) VALUES
(2, 'id', 'Log ID', 'BIGINT', 1, 1),
(2, 'username', 'Username', 'VARCHAR', 1, 1),
(2, 'action', 'Action', 'VARCHAR', 1, 1),
(2, 'resource_type', 'Resource Type', 'VARCHAR', 1, 1),
(2, 'resource_id', 'Resource ID', 'BIGINT', 1, 1),
(2, 'status', 'Status', 'VARCHAR', 1, 1),
(2, 'details', 'Details', 'JSON', 0, 0),
(2, 'timestamp', 'Timestamp', 'TIMESTAMP', 1, 1),
(2, 'ip_address', 'IP Address', 'VARCHAR', 1, 1),
(2, 'user_agent', 'User Agent', 'VARCHAR', 0, 0);

-- Fields for test_cases_view
INSERT INTO report_view_fields (view_id, field_name, display_name, field_type, is_filterable, is_sortable) VALUES
(3, 'id', 'Test Case ID', 'BIGINT', 1, 1),
(3, 'name', 'Test Case Name', 'VARCHAR', 1, 1),
(3, 'description', 'Description', 'TEXT', 0, 0),
(3, 'status', 'Status', 'VARCHAR', 1, 1),
(3, 'group_id', 'Group', 'BIGINT', 1, 1),
(3, 'tags', 'Tags', 'VARCHAR', 1, 0),
(3, 'timeout_seconds', 'Timeout (sec)', 'INT', 1, 1),
(3, 'created_by', 'Created By', 'BIGINT', 1, 1),
(3, 'created_at', 'Created At', 'TIMESTAMP', 1, 1);

SELECT 'Report view fields populated successfully' as Status;
