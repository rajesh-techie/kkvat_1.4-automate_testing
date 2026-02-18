package com.kkvat.automation.controller;

import com.kkvat.automation.dto.TestExecutionRequest;
import com.kkvat.automation.dto.TestExecutionResponse;
import com.kkvat.automation.model.TestExecution;
import com.kkvat.automation.service.TestExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/executions")
@RequiredArgsConstructor
@Tag(name = "Test Executions", description = "Test Execution Management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TestExecutionController {
    
    private final TestExecutionService testExecutionService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get all test executions with pagination")
    public ResponseEntity<Page<TestExecutionResponse>> getAllExecutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return ResponseEntity.ok(testExecutionService.getAllExecutions(pageable));
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get all test executions without pagination")
    public ResponseEntity<List<TestExecutionResponse>> getAllExecutionsList() {
        return ResponseEntity.ok(testExecutionService.getAllExecutionsList());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get test execution by ID")
    public ResponseEntity<TestExecutionResponse> getExecutionById(@PathVariable Long id) {
        return ResponseEntity.ok(testExecutionService.getExecutionById(id));
    }
    
    @GetMapping("/test-case/{testCaseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get executions by test case")
    public ResponseEntity<List<TestExecutionResponse>> getExecutionsByTestCase(
            @PathVariable Long testCaseId) {
        return ResponseEntity.ok(testExecutionService.getExecutionsByTestCase(testCaseId));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get executions by status")
    public ResponseEntity<List<TestExecutionResponse>> getExecutionsByStatus(
            @PathVariable TestExecution.Status status) {
        return ResponseEntity.ok(testExecutionService.getExecutionsByStatus(status));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    @Operation(summary = "Create a new test execution")
    public ResponseEntity<TestExecutionResponse> createExecution(
            @Valid @RequestBody TestExecutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testExecutionService.createExecution(request));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    @Operation(summary = "Update execution status")
    public ResponseEntity<TestExecutionResponse> updateExecutionStatus(
            @PathVariable Long id,
            @RequestParam TestExecution.Status status,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String resultJson) {
        return ResponseEntity.ok(testExecutionService.updateExecutionStatus(
                id, status, errorMessage, resultJson));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Delete test execution")
    public ResponseEntity<Void> deleteExecution(@PathVariable Long id) {
        testExecutionService.deleteExecution(id);
        return ResponseEntity.noContent().build();
    }
}
