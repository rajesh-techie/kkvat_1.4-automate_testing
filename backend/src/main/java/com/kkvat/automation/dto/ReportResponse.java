package com.kkvat.automation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.kkvat.automation.model.Report;
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
public class ReportResponse {
    private Long id;
    private String name;
    private String description;
    private Long viewId;
    private String viewName;
    private List<String> selectedColumns;
    private Object filterConditions;
    private Object sortConfig;
    private String reportType;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByUsername;
    private String updatedByUsername;

    public static ReportResponse from(Report report) {
        if (report == null) return null;
        
        return ReportResponse.builder()
                .id(report.getId())
                .name(report.getName())
                .description(report.getDescription())
                .viewId(report.getView().getId())
                .viewName(report.getView().getDisplayName())
                .selectedColumns(parseJsonArray(report.getSelectedColumns()))
                .filterConditions(parseJsonObject(report.getFilterConditions()))
                .sortConfig(parseJsonObject(report.getSortConfig()))
                .reportType(report.getReportType().toString())
                .isPublic(report.getIsPublic())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .createdByUsername(report.getCreatedBy() != null ? report.getCreatedBy().getUsername() : null)
                .updatedByUsername(report.getUpdatedBy() != null ? report.getUpdatedBy().getUsername() : null)
                .build();
    }

    private static List<String> parseJsonArray(String json) {
        try {
            if (json == null || json.isEmpty()) return null;
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>(){});
        } catch (Exception e) {
            return null;
        }
    }

    private static Object parseJsonObject(String json) {
        try {
            if (json == null || json.isEmpty()) return null;
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }
}
