-- Fix schema conflicts for Phase 4 - Drop old reports table and recreate with correct schema
USE kkvat_automation;

-- Drop dependent tables first (foreign key constraints)
DROP TABLE IF EXISTS report_executions;
DROP TABLE IF EXISTS report_schedules;
DROP TABLE IF EXISTS reports;

-- Now recreate with the correct Phase 4 schema
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    view_id BIGINT NOT NULL,
    selected_columns JSON NOT NULL,
    filter_conditions JSON,
    sort_config JSON,
    report_type ENUM('EXECUTION', 'USER_ACTIVITY', 'CUSTOM') NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (view_id) REFERENCES report_views(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_report_type (report_type),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB;

-- Report Schedules (Schedule report for recurring generation)
CREATE TABLE IF NOT EXISTS report_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    schedule_name VARCHAR(200) NOT NULL,
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY') NOT NULL,
    day_of_week INT NULL,
    day_of_month INT NULL,
    time_of_day TIME NOT NULL,
    email_recipients VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_executed TIMESTAMP NULL,
    next_execution TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_report_id (report_id),
    INDEX idx_is_active (is_active),
    INDEX idx_next_execution (next_execution)
) ENGINE=InnoDB;

-- Report Executions (Download reports history)
CREATE TABLE IF NOT EXISTS report_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    schedule_id BIGINT NULL,
    execution_type ENUM('MANUAL', 'SCHEDULED', 'API') NOT NULL,
    status ENUM('PENDING', 'GENERATING', 'COMPLETED', 'FAILED') NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    duration_ms BIGINT NULL,
    file_path VARCHAR(500),
    file_size BIGINT NULL,
    row_count INT NULL,
    error_message TEXT,
    executed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES report_schedules(id) ON DELETE SET NULL,
    FOREIGN KEY (executed_by) REFERENCES users(id),
    INDEX idx_report_id (report_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_executed_by (executed_by)
) ENGINE=InnoDB;

SELECT 'Phase 4 schema fixed successfully' as msg;
