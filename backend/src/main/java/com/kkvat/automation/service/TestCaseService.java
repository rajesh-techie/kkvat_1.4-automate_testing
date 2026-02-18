package com.kkvat.automation.service;

import com.kkvat.automation.dto.TestCaseRequest;
import com.kkvat.automation.dto.TestCaseResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Group;
import com.kkvat.automation.model.TestCase;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.GroupRepository;
import com.kkvat.automation.repository.TestCaseRepository;
import com.kkvat.automation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestCaseService {
    
    private final TestCaseRepository testCaseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    public Page<TestCaseResponse> getAllTestCases(Pageable pageable) {
        return testCaseRepository.findAll(pageable)
                .map(TestCaseResponse::from);
    }
    
    public List<TestCaseResponse> getAllTestCasesList() {
        return testCaseRepository.findAll().stream()
                .map(TestCaseResponse::from)
                .toList();
    }
    
    public TestCaseResponse getTestCaseById(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + id));
        return TestCaseResponse.from(testCase);
    }
    
    public TestCaseResponse createTestCase(TestCaseRequest request) {
        Long currentUserId = getCurrentUserId();
        
        TestCase testCase = TestCase.builder()
                .name(request.getName())
                .description(request.getDescription())
                .recordedActions(request.getRecordedActions())
                .status(request.getStatus())
                .tags(request.getTags())
                .baseUrl(request.getBaseUrl())
                .timeoutSeconds(request.getTimeoutSeconds())
                .createdBy(currentUserId)
                .build();
        
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.getGroupId()));
            testCase.setGroup(group);
        }
        
        TestCase savedTestCase = testCaseRepository.save(testCase);
        auditService.logSuccess("CREATE_TEST_CASE", "TestCase", savedTestCase.getId(),
                "Created test case: " + savedTestCase.getName());
        
        return TestCaseResponse.from(savedTestCase);
    }
    
    public TestCaseResponse updateTestCase(Long id, TestCaseRequest request) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + id));
        
        testCase.setName(request.getName());
        testCase.setDescription(request.getDescription());
        testCase.setRecordedActions(request.getRecordedActions());
        testCase.setStatus(request.getStatus());
        testCase.setTags(request.getTags());
        testCase.setBaseUrl(request.getBaseUrl());
        testCase.setTimeoutSeconds(request.getTimeoutSeconds());
        testCase.setUpdatedBy(getCurrentUserId());
        
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.getGroupId()));
            testCase.setGroup(group);
        } else {
            testCase.setGroup(null);
        }
        
        TestCase updatedTestCase = testCaseRepository.save(testCase);
        auditService.logSuccess("UPDATE_TEST_CASE", "TestCase", updatedTestCase.getId(),
                "Updated test case: " + updatedTestCase.getName());
        
        return TestCaseResponse.from(updatedTestCase);
    }
    
    public void deleteTestCase(Long id) {
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test case not found with id: " + id));
        
        testCaseRepository.delete(testCase);
        auditService.logSuccess("DELETE_TEST_CASE", "TestCase", id,
                "Deleted test case: " + testCase.getName());
    }
    
    public List<TestCaseResponse> searchTestCases(String keyword) {
        return testCaseRepository.findByNameContainingOrDescriptionContaining(keyword, keyword)
                .stream()
                .map(TestCaseResponse::from)
                .toList();
    }
    
    public List<TestCaseResponse> getTestCasesByStatus(TestCase.Status status) {
        return testCaseRepository.findByStatus(status).stream()
                .map(TestCaseResponse::from)
                .toList();
    }
    
    public List<TestCaseResponse> getTestCasesByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        return testCaseRepository.findByGroup(group).stream()
                .map(TestCaseResponse::from)
                .toList();
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return user.getId();
    }
}
