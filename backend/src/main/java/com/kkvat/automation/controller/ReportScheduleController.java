package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ReportScheduleRequest;
import com.kkvat.automation.dto.ReportScheduleResponse;
import com.kkvat.automation.service.ReportScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/report-schedules")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ReportScheduleController {
    private final ReportScheduleService reportScheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<Page<ReportScheduleResponse>> getAllSchedules(Pageable pageable) {
        return ResponseEntity.ok(reportScheduleService.getAllSchedules(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(reportScheduleService.getScheduleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportScheduleResponse> createSchedule(@Valid @RequestBody ReportScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportScheduleService.createSchedule(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER')")
    public ResponseEntity<ReportScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ReportScheduleRequest request) {
        return ResponseEntity.ok(reportScheduleService.updateSchedule(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        reportScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
