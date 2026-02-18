package com.kkvat.automation.repository;

import com.kkvat.automation.model.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByReportId(Long reportId);
    List<ReportSchedule> findByIsActiveTrue();
    List<ReportSchedule> findByIsActiveTrueAndNextExecutionBefore(LocalDateTime dateTime);
    List<ReportSchedule> findByCreatedById(Long createdById);
}
