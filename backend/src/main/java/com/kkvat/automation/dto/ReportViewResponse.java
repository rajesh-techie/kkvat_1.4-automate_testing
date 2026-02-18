package com.kkvat.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportViewResponse {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String tableName;
    private Boolean isActive;
    private List<ReportViewFieldResponse> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportViewFieldResponse {
        private Long id;
        private String fieldName;
        private String displayName;
        private String fieldType;
        private Boolean isFilterable;
        private Boolean isSortable;
    }
}
