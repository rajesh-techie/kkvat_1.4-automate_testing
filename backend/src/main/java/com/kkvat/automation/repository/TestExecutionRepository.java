package com.kkvat.automation.repository;

import com.kkvat.automation.model.TestCase;
import com.kkvat.automation.model.TestExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    
    List<TestExecution> findByTestCaseId(Long testCaseId);
    
    Page<TestExecution> findByTestCaseId(Long testCaseId, Pageable pageable);
    
    List<TestExecution> findByTestCaseOrderByStartTimeDesc(TestCase testCase);
    
    List<TestExecution> findByStatus(TestExecution.Status status);
    
    List<TestExecution> findByExecutedBy(Long executedBy);
    
    @Query("SELECT te FROM TestExecution te WHERE te.startTime BETWEEN :start AND :end")
    List<TestExecution> findByDateRange(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT te FROM TestExecution te WHERE te.testCase.id = :testCaseId ORDER BY te.startTime DESC")
    List<TestExecution> findLatestExecutions(Long testCaseId, Pageable pageable);
}
