package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ReportExecutionResponse;
import com.kkvat.automation.service.ReportExecutionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/report-executions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ReportExecutionController {
    private final ReportExecutionService reportExecutionService;

    @GetMapping("/report/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportExecutionResponse>> getExecutionsByReport(
            @PathVariable Long reportId,
            Pageable pageable) {
        return ResponseEntity.ok(reportExecutionService.getExecutionsByReport(reportId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<ReportExecutionResponse> getExecutionById(@PathVariable Long id) {
        return ResponseEntity.ok(reportExecutionService.getExecutionById(id));
    }

    @GetMapping("/my-executions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportExecutionResponse>> getMyExecutions(Pageable pageable) {
        return ResponseEntity.ok(reportExecutionService.getMyExecutions(pageable));
    }

    @PostMapping("/generate/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportExecutionResponse> generateReport(@PathVariable Long reportId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(reportExecutionService.generateReport(reportId));
    }

    @GetMapping("/download/{executionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long executionId) throws Exception {
        ReportExecutionResponse execution = reportExecutionService.getExecutionById(executionId);

        if (execution.getFilePath() == null || execution.getStatus().equals("PENDING") || execution.getStatus().equals("GENERATING")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (execution.getStatus().equals("FAILED")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        File file = new File(execution.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] fileContent = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                .body(fileContent);
    }

    @GetMapping("/download-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
    public ResponseEntity<Page<ReportExecutionResponse>> getDownloadableReports(Pageable pageable) {
        return ResponseEntity.ok(reportExecutionService.getMyExecutions(pageable));
    }

    @PostMapping("/run/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<List<Map<String, Object>>> runReportNow(@PathVariable Long reportId,
                                                                  @RequestBody(required = false) Map<String, Object> filters) {
        List<Map<String, Object>> rows = reportExecutionService.runReportNow(reportId, filters);
        return ResponseEntity.ok(rows);
    }
}
