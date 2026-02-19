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
    private final com.kkvat.automation.repository.ReportViewFieldRepository reportViewFieldRepository;
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
        try {
            log.debug("createReport request: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize ReportRequest for logging", e);
        }
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
                    .selectedColumns(request.getSelectedColumns() != null ? objectMapper.writeValueAsString(request.getSelectedColumns()) : null)
                    .filterConditions(request.getFilterConditions() != null ? serializeFilterConditionKeys(request.getFilterConditions(), view) : null)
                    .sortConfig(request.getSortConfig() != null ? objectMapper.writeValueAsString(request.getSortConfig()) : null)
                    .reportType(Report.ReportType.valueOf(request.getReportType()))
                    .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                    .createdBy(user)
                    .build();

            Report saved = reportRepository.save(report);
            try {
                log.debug("Saved report id={}, selectedColumns={}, filterConditions={}, sortConfig={}",
                        saved.getId(), saved.getSelectedColumns(), saved.getFilterConditions(), saved.getSortConfig());
            } catch (Exception e) {
                log.warn("Failed to log saved report JSON fields", e);
            }
            auditService.logSuccess("CREATE", "REPORT", saved.getId(), "Created report: " + request.getName());
            return ReportResponse.from(saved);
        } catch (Exception e) {
            log.error("Error creating report", e);
            auditService.logFailure("CREATE", "REPORT", 0L, e.getMessage());
            throw new RuntimeException("Failed to create report: " + e.getMessage());
        }
    }

    public ReportResponse updateReport(Long id, ReportRequest request) {
        try {
            log.debug("updateReport request: {}", objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            log.warn("Failed to serialize ReportRequest for logging", e);
        }
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
            report.setSelectedColumns(request.getSelectedColumns() != null ? objectMapper.writeValueAsString(request.getSelectedColumns()) : null);
            report.setFilterConditions(request.getFilterConditions() != null ? serializeFilterConditionKeys(request.getFilterConditions(), view) : null);
            report.setSortConfig(request.getSortConfig() != null ? objectMapper.writeValueAsString(request.getSortConfig()) : null);
            report.setReportType(Report.ReportType.valueOf(request.getReportType()));
            report.setIsPublic(request.getIsPublic());
            report.setUpdatedBy(user);

            Report updated = reportRepository.save(report);
                try {
                    log.debug("Updated report id={}, selectedColumns={}, filterConditions={}, sortConfig={}",
                            updated.getId(), updated.getSelectedColumns(), updated.getFilterConditions(), updated.getSortConfig());
                } catch (Exception e) {
                    log.warn("Failed to log updated report JSON fields", e);
                }
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

    public ReportResponse getLatestReport() {
        Report latest = reportRepository.findTopByOrderByIdDesc();
        if (latest == null) {
            throw new ResourceNotFoundException("No reports found");
        }
        return ReportResponse.from(latest);
    }

    public java.util.Map<String, Object> getLatestReportRaw() {
        Report latest = reportRepository.findTopByOrderByIdDesc();
        if (latest == null) {
            throw new ResourceNotFoundException("No reports found");
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", latest.getId());
        map.put("name", latest.getName());
        map.put("selectedColumnsRaw", latest.getSelectedColumns());
        map.put("filterConditionsRaw", latest.getFilterConditions());
        map.put("sortConfigRaw", latest.getSortConfig());
        return map;
    }

    public ReportResponse insertSampleReport(Long viewId) {
        ReportView view = reportViewRepository.findById(viewId)
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with id: " + viewId));
        // use admin user for debug insert
        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("User 'admin' not found"));

        try {
                java.util.Map<String,Object> simulatedMap = java.util.Map.of("active", true);
                Report report = Report.builder()
                    .name("debug-sample")
                    .description("Inserted by debug endpoint")
                    .view(view)
                    .selectedColumns(objectMapper.writeValueAsString(java.util.List.of("id", "username", "email")))
                    .filterConditions(serializeFilterConditionKeys(simulatedMap, view))
                    .sortConfig(objectMapper.writeValueAsString(java.util.List.of(java.util.Map.of("column", "id", "direction", "DESC"))))
                    .reportType(Report.ReportType.CUSTOM)
                    .isPublic(false)
                    .createdBy(admin)
                    .build();

            Report saved = reportRepository.save(report);
            return ReportResponse.from(saved);
        } catch (Exception e) {
            log.error("Failed to insert sample report", e);
            throw new RuntimeException(e);
        }
    }

    private String serializeFilterConditionKeys(Object filterConditions, ReportView view) {
        try {
            // Build mapping of possible incoming keys (displayName and fieldName) -> actual fieldName
            java.util.List<com.kkvat.automation.model.ReportViewField> fields = reportViewFieldRepository.findByViewId(view.getId());
            java.util.Map<String, String> keyToField = new java.util.HashMap<>();
            for (com.kkvat.automation.model.ReportViewField f : fields) {
                if (f.getDisplayName() != null) keyToField.put(f.getDisplayName().toLowerCase(), f.getFieldName());
                if (f.getFieldName() != null) keyToField.put(f.getFieldName().toLowerCase(), f.getFieldName());
            }

            if (filterConditions instanceof java.util.Map) {
                java.util.Map<?,?> m = (java.util.Map<?,?>) filterConditions;
                java.util.List<String> keys = new java.util.ArrayList<>();
                for (Object k : m.keySet()) {
                    String keyStr = String.valueOf(k);
                    String mapped = keyToField.getOrDefault(keyStr.toLowerCase(), keyStr);
                    keys.add(mapped);
                }
                return objectMapper.writeValueAsString(keys);
            } else if (filterConditions instanceof java.util.Collection) {
                java.util.List<String> keys = new java.util.ArrayList<>();
                for (Object v : (java.util.Collection<?>) filterConditions) {
                    String keyStr = String.valueOf(v);
                    String mapped = keyToField.getOrDefault(keyStr.toLowerCase(), keyStr);
                    keys.add(mapped);
                }
                return objectMapper.writeValueAsString(keys);
            } else {
                try {
                    java.util.Map<?,?> m = objectMapper.readValue(String.valueOf(filterConditions), java.util.Map.class);
                    java.util.List<String> keys = new java.util.ArrayList<>();
                    for (Object k : m.keySet()) {
                        String keyStr = String.valueOf(k);
                        String mapped = keyToField.getOrDefault(keyStr.toLowerCase(), keyStr);
                        keys.add(mapped);
                    }
                    return objectMapper.writeValueAsString(keys);
                } catch (Exception ex) {
                    String keyStr = String.valueOf(filterConditions);
                    String mapped = keyToField.getOrDefault(keyStr.toLowerCase(), keyStr);
                    return objectMapper.writeValueAsString(java.util.List.of(mapped));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to serialize filterConditions to keys", e);
            try { return objectMapper.writeValueAsString(java.util.List.of()); } catch (Exception ex) { return "[]"; }
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
