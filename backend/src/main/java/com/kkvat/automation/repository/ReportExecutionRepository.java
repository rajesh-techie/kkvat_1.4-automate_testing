package com.kkvat.automation.repository;

import com.kkvat.automation.model.ReportExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {
    List<ReportExecution> findByReportId(Long reportId);
    Page<ReportExecution> findByReportIdOrderByCreatedAtDesc(Long reportId, Pageable pageable);
    List<ReportExecution> findByScheduleId(Long scheduleId);
    List<ReportExecution> findByStatusAndCreatedAtBetween(ReportExecution.Status status, LocalDateTime start, LocalDateTime end);
    Page<ReportExecution> findByExecutedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
