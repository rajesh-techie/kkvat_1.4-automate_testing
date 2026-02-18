package com.kkvat.automation.dto;

import com.kkvat.automation.model.ReportSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportScheduleResponse {
    private Long id;
    private Long reportId;
    private String reportName;
    private String scheduleName;
    private String frequency;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private LocalTime timeOfDay;
    private String emailRecipients;
    private Boolean isActive;
    private LocalDateTime lastExecuted;
    private LocalDateTime nextExecution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByUsername;
    private String updatedByUsername;

    public static ReportScheduleResponse from(ReportSchedule schedule) {
        if (schedule == null) return null;
        
        return ReportScheduleResponse.builder()
                .id(schedule.getId())
                .reportId(schedule.getReport().getId())
                .reportName(schedule.getReport().getName())
                .scheduleName(schedule.getScheduleName())
                .frequency(schedule.getFrequency().toString())
                .dayOfWeek(schedule.getDayOfWeek())
                .dayOfMonth(schedule.getDayOfMonth())
                .timeOfDay(schedule.getTimeOfDay())
                .emailRecipients(schedule.getEmailRecipients())
                .isActive(schedule.getIsActive())
                .lastExecuted(schedule.getLastExecuted())
                .nextExecution(schedule.getNextExecution())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .createdByUsername(schedule.getCreatedBy() != null ? schedule.getCreatedBy().getUsername() : null)
                .updatedByUsername(schedule.getUpdatedBy() != null ? schedule.getUpdatedBy().getUsername() : null)
                .build();
    }
}
