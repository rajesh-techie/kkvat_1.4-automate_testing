package com.kkvat.automation.service;

import com.kkvat.automation.dto.TestExecutionRequest;
import com.kkvat.automation.dto.TestExecutionResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.TestCase;
import com.kkvat.automation.model.TestExecution;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.TestCaseRepository;
import com.kkvat.automation.repository.TestExecutionRepository;
import com.kkvat.automation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestExecutionService {
    
    private final TestExecutionRepository testExecutionRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    public Page<TestExecutionResponse> getAllExecutions(Pageable pageable) {
        return testExecutionRepository.findAll(pageable)
                .map(TestExecutionResponse::from);
    }
    
    public List<TestExecutionResponse> getAllExecutionsList() {
        return testExecutionRepository.findAll().stream()
                .map(TestExecutionResponse::from)
                .toList();
    }
    
    public TestExecutionResponse getExecutionById(Long id) {
        TestExecution execution = testExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test execution not found with id: " + id));
        return TestExecutionResponse.from(execution);
    }
    
    public List<TestExecutionResponse> getExecutionsByTestCase(Long testCaseId) {
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + testCaseId));
        
        return testExecutionRepository.findByTestCaseOrderByStartTimeDesc(testCase).stream()
                .map(TestExecutionResponse::from)
                .toList();
    }
    
    public List<TestExecutionResponse> getExecutionsByStatus(TestExecution.Status status) {
        return testExecutionRepository.findByStatus(status).stream()
                .map(TestExecutionResponse::from)
                .toList();
    }
    
    public TestExecutionResponse createExecution(TestExecutionRequest request) {
        Long currentUserId = getCurrentUserId();
        
        TestCase testCase = testCaseRepository.findById(request.getTestCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + request.getTestCaseId()));
        
        TestExecution execution = TestExecution.builder()
                .testCase(testCase)
                .status(TestExecution.Status.RUNNING)
                .startTime(LocalDateTime.now())
                .executedBy(currentUserId)
                .browser(request.getBrowser())
                .build();
        
        TestExecution savedExecution = testExecutionRepository.save(execution);
        auditService.logSuccess("CREATE_EXECUTION", "TestExecution", savedExecution.getId(), 
                "Created test execution for: " + testCase.getName());
        
        return TestExecutionResponse.from(savedExecution);
    }
    
    public TestExecutionResponse updateExecutionStatus(Long id, TestExecution.Status status, 
                                                      String errorMessage, String resultJson) {
        TestExecution execution = testExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test execution not found with id: " + id));
        
        execution.setStatus(status);
        if (errorMessage != null) {
            execution.setErrorMessage(errorMessage);
        }
        if (resultJson != null) {
            execution.setResultJson(resultJson);
        }
        
        if (status == TestExecution.Status.PASSED || status == TestExecution.Status.FAILED) {
            execution.setEndTime(LocalDateTime.now());
            if (execution.getStartTime() != null) {
                long durationMs = java.time.Duration.between(
                        execution.getStartTime(), execution.getEndTime()).toMillis();
                execution.setDurationMs(durationMs);
            }
        }
        
        TestExecution updatedExecution = testExecutionRepository.save(execution);
        auditService.logSuccess("UPDATE_EXECUTION", "TestExecution", updatedExecution.getId(),
                "Updated execution status to: " + status);
        
        return TestExecutionResponse.from(updatedExecution);
    }
    
    public void deleteExecution(Long id) {
        TestExecution execution = testExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test execution not found with id: " + id));
        
        testExecutionRepository.delete(execution);
        auditService.logSuccess("DELETE_EXECUTION", "TestExecution", id, 
                "Deleted test execution: " + id);
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return user.getId();
    }
}
