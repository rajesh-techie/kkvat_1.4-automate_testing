package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ReportViewResponse;
import com.kkvat.automation.service.ReportViewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report-views")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER', 'TESTER', 'VIEWER')")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportViewController {
    private final ReportViewService reportViewService;

    @GetMapping
    public ResponseEntity<List<ReportViewResponse>> getAllViews() {
        return ResponseEntity.ok(reportViewService.getAllViews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportViewResponse> getViewById(@PathVariable Long id) {
        return ResponseEntity.ok(reportViewService.getViewById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ReportViewResponse> getViewByName(@PathVariable String name) {
        return ResponseEntity.ok(reportViewService.getViewByName(name));
    }

    @GetMapping("/{viewId}/fields")
    public ResponseEntity<List<ReportViewResponse.ReportViewFieldResponse>> getViewFields(@PathVariable Long viewId) {
        return ResponseEntity.ok(reportViewService.getViewFields(viewId));
    }

    @GetMapping("/{viewId}/fields/filterable")
    public ResponseEntity<List<ReportViewResponse.ReportViewFieldResponse>> getFilterableFields(@PathVariable Long viewId) {
        return ResponseEntity.ok(reportViewService.getFilterableFields(viewId));
    }

    @GetMapping("/{viewId}/fields/sortable")
    public ResponseEntity<List<ReportViewResponse.ReportViewFieldResponse>> getSortableFields(@PathVariable Long viewId) {
        return ResponseEntity.ok(reportViewService.getSortableFields(viewId));
    }

    @GetMapping("/{viewId}/columns")
    public ResponseEntity<List<com.kkvat.automation.dto.ColumnInfoResponse>> getViewColumns(@PathVariable Long viewId,
                                                                                              @RequestParam(value = "schema", required = false) String schema) {
        if (schema == null || schema.isBlank()) schema = "kkvat_automation";
        return ResponseEntity.ok(reportViewService.getColumnsForView(viewId, schema));
    }

    @GetMapping("/name/{name}/columns")
    public ResponseEntity<List<com.kkvat.automation.dto.ColumnInfoResponse>> getViewColumnsByName(@PathVariable String name,
                                                                                                     @RequestParam(value = "schema", required = false) String schema) {
        if (schema == null || schema.isBlank()) schema = "kkvat_automation";
        return ResponseEntity.ok(reportViewService.getColumnsForViewByName(name, schema));
    }
}
