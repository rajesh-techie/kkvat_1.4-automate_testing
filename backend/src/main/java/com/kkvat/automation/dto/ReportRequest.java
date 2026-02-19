package com.kkvat.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"view_id"})
    private Long viewId;

    @NotNull(message = "Selected columns are required")
    @JsonAlias({"selected_columns"})
    private List<String> selectedColumns; // column names to include

    @JsonAlias({"filtered_conditions"})
    private Object filterConditions; // filter rules as JSON object

    @JsonAlias({"sort_config"})
    private Object sortConfig; // sort configuration as JSON object

    @NotNull(message = "Report type is required")
    private String reportType; // EXECUTION, USER_ACTIVITY, CUSTOM
    @JsonAlias({"is_public"})
    private Boolean isPublic = false;

    
}
