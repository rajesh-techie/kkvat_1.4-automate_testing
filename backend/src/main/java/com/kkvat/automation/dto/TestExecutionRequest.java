package com.kkvat.automation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TestExecutionRequest {
    
    @NotNull(message = "Test case ID is required")
    private Long testCaseId;
    
    @NotBlank(message = "Browser type is required")
    @Pattern(regexp = "^(chromium|firefox|webkit)$", 
             message = "Browser must be one of: chromium, firefox, webkit")
    private String browser = "chromium";
    
    private Boolean headless = false;
    
    private Boolean recordVideo = false;
    
    private Boolean captureScreenshots = true;
}
