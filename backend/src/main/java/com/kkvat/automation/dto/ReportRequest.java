package com.kkvat.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    @NotBlank(message = "Report name is required")
    private String name;

    private String description;

    @NotNull(message = "View ID is required")
    private Long viewId;

    @NotNull(message = "Selected columns are required")
    private List<String> selectedColumns; // column names to include

    private Object filterConditions; // filter rules as JSON object

    private Object sortConfig; // sort configuration as JSON object

    @NotNull(message = "Report type is required")
    private String reportType; // EXECUTION, USER_ACTIVITY, CUSTOM

    private Boolean isPublic = false;
}
