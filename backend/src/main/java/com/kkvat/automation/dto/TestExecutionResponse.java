package com.kkvat.automation.dto;

import com.kkvat.automation.model.TestExecution;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestExecutionResponse {
    
    private Long id;
    private Long testCaseId;
    private String testCaseName;
    private TestExecution.Status status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String resultJson;
    private String errorMessage;
    private String screenshotsPath;
    private String videoPath;
    private Long executedBy;
    private String executedByUsername;
    private String browser;
    
    public static TestExecutionResponse from(TestExecution execution) {
        TestExecutionResponseBuilder builder = TestExecutionResponse.builder()
                .id(execution.getId())
                .status(execution.getStatus())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .durationMs(execution.getDurationMs())
                .resultJson(execution.getResultJson())
                .errorMessage(execution.getErrorMessage())
                .screenshotsPath(execution.getScreenshotsPath())
                .videoPath(execution.getVideoPath())
                .executedBy(execution.getExecutedBy())
                .browser(execution.getBrowser());
        
        if (execution.getTestCase() != null) {
            builder.testCaseId(execution.getTestCase().getId())
                   .testCaseName(execution.getTestCase().getName());
        }
        
        return builder.build();
    }
}
