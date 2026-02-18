package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ReportRequest;
import com.kkvat.automation.dto.ReportResponse;
import com.kkvat.automation.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportResponse>> getAllReports(Pageable pageable) {
        return ResponseEntity.ok(reportService.getAllReports(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportResponse> createReport(@RequestBody ReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long id,
            @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.updateReport(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportResponse>> searchReports(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(reportService.searchReports(keyword, pageable));
    }

    @GetMapping("/view/{viewId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportResponse>> getReportsByView(
            @PathVariable Long viewId,
            Pageable pageable) {
        // Convert to Page<ReportResponse>
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                reportService.getReportsByView(viewId),
                pageable,
                reportService.getReportsByView(viewId).size()
        ));
    }
}
