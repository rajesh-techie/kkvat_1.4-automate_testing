package com.kkvat.automation.controller;

import com.kkvat.automation.dto.TestCaseRequest;
import com.kkvat.automation.dto.TestCaseResponse;
import com.kkvat.automation.model.TestCase;
import com.kkvat.automation.service.TestCaseService;
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
@RequestMapping("/test-cases")
@RequiredArgsConstructor
@Tag(name = "Test Cases", description = "Test Case Management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TestCaseController {
    
    private final TestCaseService testCaseService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get all test cases with pagination")
    public ResponseEntity<Page<TestCaseResponse>> getAllTestCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(testCaseService.getAllTestCases(pageable));
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get all test cases without pagination")
    public ResponseEntity<List<TestCaseResponse>> getAllTestCasesList() {
        return ResponseEntity.ok(testCaseService.getAllTestCasesList());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get test case by ID")
    public ResponseEntity<TestCaseResponse> getTestCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(testCaseService.getTestCaseById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    @Operation(summary = "Create a new test case")
    public ResponseEntity<TestCaseResponse> createTestCase(@Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testCaseService.createTestCase(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    @Operation(summary = "Update test case")
    public ResponseEntity<TestCaseResponse> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseRequest request) {
        return ResponseEntity.ok(testCaseService.updateTestCase(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Delete test case")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Search test cases by keyword")
    public ResponseEntity<List<TestCaseResponse>> searchTestCases(@RequestParam String keyword) {
        return ResponseEntity.ok(testCaseService.searchTestCases(keyword));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get test cases by status")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByStatus(
            @PathVariable TestCase.Status status) {
        return ResponseEntity.ok(testCaseService.getTestCasesByStatus(status));
    }
    
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    @Operation(summary = "Get test cases by group")
    public ResponseEntity<List<TestCaseResponse>> getTestCasesByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(testCaseService.getTestCasesByGroup(groupId));
    }
}
