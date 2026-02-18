package com.kkvat.automation.dto;

import com.kkvat.automation.model.TestCase;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestCaseResponse {
    
    private Long id;
    private String name;
    private String description;
    private String recordedActions;
    private TestCase.Status status;
    private Long groupId;
    private String groupName;
    private String tags;
    private String baseUrl;
    private Integer timeoutSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String createdByUsername;
    private Long updatedBy;
    private String updatedByUsername;
    
    public static TestCaseResponse from(TestCase testCase) {
        TestCaseResponseBuilder builder = TestCaseResponse.builder()
                .id(testCase.getId())
                .name(testCase.getName())
                .description(testCase.getDescription())
                .recordedActions(testCase.getRecordedActions())
                .status(testCase.getStatus())
                .tags(testCase.getTags())
                .baseUrl(testCase.getBaseUrl())
                .timeoutSeconds(testCase.getTimeoutSeconds())
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .createdBy(testCase.getCreatedBy())
                .updatedBy(testCase.getUpdatedBy());
        
        if (testCase.getGroup() != null) {
            builder.groupId(testCase.getGroup().getId())
                   .groupName(testCase.getGroup().getName());
        }
        
        return builder.build();
    }
}
