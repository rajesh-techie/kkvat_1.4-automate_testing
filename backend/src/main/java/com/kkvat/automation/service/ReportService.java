package com.kkvat.automation.service;

import com.kkvat.automation.dto.ReportRequest;
import com.kkvat.automation.dto.ReportResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Report;
import com.kkvat.automation.model.ReportView;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.ReportRepository;
import com.kkvat.automation.repository.ReportViewRepository;
import com.kkvat.automation.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportViewRepository reportViewRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public Page<ReportResponse> getAllReports(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<Report> reports = reportRepository.findByCreatedByIdOrIsPublicTrue(userId, pageable);
        return reports.map(ReportResponse::from);
    }

    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return ReportResponse.from(report);
    }

    public ReportResponse createReport(ReportRequest request) {
        ReportView view = reportViewRepository.findById(request.getViewId())
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with id: " + request.getViewId()));

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            Report report = Report.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .view(view)
                    .selectedColumns(objectMapper.writeValueAsString(request.getSelectedColumns()))
                    .filterConditions(request.getFilterConditions() != null ? objectMapper.writeValueAsString(request.getFilterConditions()) : null)
                    .sortConfig(request.getSortConfig() != null ? objectMapper.writeValueAsString(request.getSortConfig()) : null)
                    .reportType(Report.ReportType.valueOf(request.getReportType()))
                    .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                    .createdBy(user)
                    .build();

            Report saved = reportRepository.save(report);
            auditService.logSuccess("CREATE", "REPORT", saved.getId(), "Created report: " + request.getName());
            return ReportResponse.from(saved);
        } catch (Exception e) {
            log.error("Error creating report", e);
            auditService.logFailure("CREATE", "REPORT", 0L, e.getMessage());
            throw new RuntimeException("Failed to create report: " + e.getMessage());
        }
    }

    public ReportResponse updateReport(Long id, ReportRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        ReportView view = reportViewRepository.findById(request.getViewId())
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found"));

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            report.setName(request.getName());
            report.setDescription(request.getDescription());
            report.setView(view);
            report.setSelectedColumns(objectMapper.writeValueAsString(request.getSelectedColumns()));
            report.setFilterConditions(request.getFilterConditions() != null ? objectMapper.writeValueAsString(request.getFilterConditions()) : null);
            report.setSortConfig(request.getSortConfig() != null ? objectMapper.writeValueAsString(request.getSortConfig()) : null);
            report.setReportType(Report.ReportType.valueOf(request.getReportType()));
            report.setIsPublic(request.getIsPublic());
            report.setUpdatedBy(user);

            Report updated = reportRepository.save(report);
            auditService.logSuccess("UPDATE", "REPORT", updated.getId(), "Updated report: " + request.getName());
            return ReportResponse.from(updated);
        } catch (Exception e) {
            log.error("Error updating report", e);
            auditService.logFailure("UPDATE", "REPORT", id, e.getMessage());
            throw new RuntimeException("Failed to update report: " + e.getMessage());
        }
    }

    public void deleteReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        
        reportRepository.delete(report);
        auditService.logSuccess("DELETE", "REPORT", id, "Deleted report: " + report.getName());
    }

    public Page<ReportResponse> searchReports(String keyword, Pageable pageable) {
        Page<Report> reports = reportRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
        return reports.map(ReportResponse::from);
    }

    public List<ReportResponse> getReportsByView(Long viewId) {
        List<Report> reports = reportRepository.findByViewId(viewId);
        return reports.stream().map(ReportResponse::from).collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
