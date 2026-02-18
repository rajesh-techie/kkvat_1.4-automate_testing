package com.kkvat.automation.dto;

import com.kkvat.automation.model.ReportExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExecutionResponse {
    private Long id;
    private Long reportId;
    private String reportName;
    private Long scheduleId;
    private String executionType; // MANUAL, SCHEDULED, API
    private String status; // PENDING, GENERATING, COMPLETED, FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String filePath;
    private Long fileSize;
    private Integer rowCount;
    private String errorMessage;
    private String executedByUsername;
    private LocalDateTime createdAt;

    public static ReportExecutionResponse from(ReportExecution execution) {
        if (execution == null) return null;
        
        return ReportExecutionResponse.builder()
                .id(execution.getId())
                .reportId(execution.getReport().getId())
                .reportName(execution.getReport().getName())
                .scheduleId(execution.getSchedule() != null ? execution.getSchedule().getId() : null)
                .executionType(execution.getExecutionType().toString())
                .status(execution.getStatus().toString())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .durationMs(execution.getDurationMs())
                .filePath(execution.getFilePath())
                .fileSize(execution.getFileSize())
                .rowCount(execution.getRowCount())
                .errorMessage(execution.getErrorMessage())
                .executedByUsername(execution.getExecutedBy() != null ? execution.getExecutedBy().getUsername() : null)
                .createdAt(execution.getCreatedAt())
                .build();
    }
}
