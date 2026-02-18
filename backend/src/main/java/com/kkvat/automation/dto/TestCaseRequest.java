package com.kkvat.automation.dto;

import com.kkvat.automation.model.TestCase;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TestCaseRequest {
    
    @NotBlank(message = "Test case name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    @NotBlank(message = "Recorded actions are required")
    private String recordedActions;
    
    @NotNull(message = "Status is required")
    private TestCase.Status status;
    
    private Long groupId;
    
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;
    
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    private String baseUrl;
    
    @Min(value = 1, message = "Timeout must be at least 1 second")
    @Max(value = 600, message = "Timeout must not exceed 600 seconds")
    private Integer timeoutSeconds = 30;
}
