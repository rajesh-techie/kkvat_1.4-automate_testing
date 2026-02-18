package com.kkvat.automation.service;

import com.kkvat.automation.dto.ReportExecutionResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Report;
import com.kkvat.automation.model.ReportExecution;
import com.kkvat.automation.model.ReportSchedule;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.ReportExecutionRepository;
import com.kkvat.automation.repository.ReportRepository;
import com.kkvat.automation.repository.ReportScheduleRepository;
import com.kkvat.automation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportExecutionService {
    private final ReportExecutionRepository reportExecutionRepository;
    private final ReportRepository reportRepository;
    private final ReportScheduleRepository reportScheduleRepository;
    private final UserRepository userRepository;
    private final ReportGenerationService reportGenerationService;
    private final AuditService auditService;

    public Page<ReportExecutionResponse> getExecutionsByReport(Long reportId, Pageable pageable) {
        Page<ReportExecution> executions = reportExecutionRepository.findByReportIdOrderByCreatedAtDesc(reportId, pageable);
        return executions.map(ReportExecutionResponse::from);
    }

    public ReportExecutionResponse getExecutionById(Long id) {
        ReportExecution execution = reportExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found with id: " + id));
        return ReportExecutionResponse.from(execution);
    }

    public Page<ReportExecutionResponse> getMyExecutions(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<ReportExecution> executions = reportExecutionRepository.findByExecutedByIdOrderByCreatedAtDesc(userId, pageable);
        return executions.map(ReportExecutionResponse::from);
    }

    public ReportExecutionResponse generateReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReportExecution execution = ReportExecution.builder()
                .report(report)
                .executionType(ReportExecution.ExecutionType.MANUAL)
                .status(ReportExecution.Status.PENDING)
                .startTime(LocalDateTime.now())
                .executedBy(user)
                .build();

        ReportExecution saved = reportExecutionRepository.save(execution);
        
        // Trigger async report generation
        generateReportAsync(saved.getId(), report, user);
        
        auditService.logSuccess("EXECUTE", "REPORT", reportId, "Triggered manual report generation");
        return ReportExecutionResponse.from(saved);
    }

    public ReportExecutionResponse generateScheduledReport(Long scheduleId) {
        ReportSchedule schedule = reportScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        Report report = schedule.getReport();
        Long userId = schedule.getCreatedBy().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReportExecution execution = ReportExecution.builder()
                .report(report)
                .schedule(schedule)
                .executionType(ReportExecution.ExecutionType.SCHEDULED)
                .status(ReportExecution.Status.PENDING)
                .startTime(LocalDateTime.now())
                .executedBy(user)
                .build();

        ReportExecution saved = reportExecutionRepository.save(execution);
        
        // Update schedule's last executed time
        schedule.setLastExecuted(LocalDateTime.now());
        reportScheduleRepository.save(schedule);
        
        // Trigger async report generation
        generateReportAsync(saved.getId(), report, user);
        
        auditService.logSuccess("EXECUTE", "REPORT_SCHEDULE", scheduleId, "Triggered scheduled report generation");
        return ReportExecutionResponse.from(saved);
    }

    @Async
    public void generateReportAsync(Long executionId, Report report, User user) {
        try {
            ReportExecution execution = reportExecutionRepository.findById(executionId)
                    .orElseThrow();

            execution.setStatus(ReportExecution.Status.GENERATING);
            reportExecutionRepository.save(execution);

            // Generate CSV report
            ReportGenerationService.ReportResult result = reportGenerationService.generateCsvReport(report, user);

            // Update execution with results
            execution.setStatus(ReportExecution.Status.COMPLETED);
            execution.setEndTime(LocalDateTime.now());
            long durationMs = java.time.temporal.ChronoUnit.MILLIS.between(execution.getStartTime(), execution.getEndTime());
            execution.setDurationMs(durationMs);
            execution.setFilePath(result.getFilePath());
            execution.setFileSize(result.getFileSize());
            execution.setRowCount(result.getRowCount());

            reportExecutionRepository.save(execution);
            
            auditService.logSuccess("GENERATE", "REPORT", report.getId(), 
                    "Report generated successfully: " + result.getRowCount() + " rows");

        } catch (Exception e) {
            log.error("Error generating report", e);
            ReportExecution execution = reportExecutionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setStatus(ReportExecution.Status.FAILED);
                execution.setEndTime(LocalDateTime.now());
                execution.setErrorMessage(e.getMessage());
                reportExecutionRepository.save(execution);
            }
            auditService.logFailure("GENERATE", "REPORT", report.getId(), e.getMessage());
        }
    }

    public void markExecutionFailed(Long executionId, String errorMessage) {
        ReportExecution execution = reportExecutionRepository.findById(executionId)
                .orElseThrow();
        execution.setStatus(ReportExecution.Status.FAILED);
        execution.setEndTime(LocalDateTime.now());
        execution.setErrorMessage(errorMessage);
        reportExecutionRepository.save(execution);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
