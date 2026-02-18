package com.kkvat.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportScheduleRequest {
    @NotNull(message = "Report ID is required")
    private Long reportId;

    @NotBlank(message = "Schedule name is required")
    private String scheduleName;

    @NotNull(message = "Frequency is required")
    private String frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY

    private Integer dayOfWeek; // 0=Sunday, 1=Monday, etc. (for WEEKLY)

    private Integer dayOfMonth; // 1-31 (for MONTHLY, QUARTERLY, ANNUALLY)

    @NotNull(message = "Time of day is required")
    private LocalTime timeOfDay; // HH:mm:ss

    private String emailRecipients; // comma-separated emails

    private Boolean isActive = true;
}
