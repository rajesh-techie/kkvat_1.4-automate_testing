package com.kkvat.automation.service;

import com.kkvat.automation.dto.ReportViewResponse;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.ReportView;
import com.kkvat.automation.model.ReportViewField;
import com.kkvat.automation.repository.ReportViewRepository;
import com.kkvat.automation.repository.ReportViewFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportViewService {
    private final ReportViewRepository reportViewRepository;
    private final ReportViewFieldRepository reportViewFieldRepository;

    public List<ReportViewResponse> getAllViews() {
        List<ReportView> views = reportViewRepository.findByIsActiveTrue();
        return views.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ReportViewResponse getViewById(Long id) {
        ReportView view = reportViewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with id: " + id));
        return convertToResponse(view);
    }

    public ReportViewResponse getViewByName(String name) {
        ReportView view = reportViewRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with name: " + name));
        return convertToResponse(view);
    }

    public List<ReportViewResponse.ReportViewFieldResponse> getViewFields(Long viewId) {
        List<ReportViewField> fields = reportViewFieldRepository.findByViewId(viewId);
        return fields.stream()
                .map(this::convertFieldToResponse)
                .collect(Collectors.toList());
    }

    public List<ReportViewResponse.ReportViewFieldResponse> getFilterableFields(Long viewId) {
        List<ReportViewField> fields = reportViewFieldRepository.findByViewIdAndIsFilterableTrue(viewId);
        return fields.stream()
                .map(this::convertFieldToResponse)
                .collect(Collectors.toList());
    }

    public List<ReportViewResponse.ReportViewFieldResponse> getSortableFields(Long viewId) {
        List<ReportViewField> fields = reportViewFieldRepository.findByViewIdAndIsSortableTrue(viewId);
        return fields.stream()
                .map(this::convertFieldToResponse)
                .collect(Collectors.toList());
    }

    private ReportViewResponse convertToResponse(ReportView view) {
        List<ReportViewField> fields = reportViewFieldRepository.findByViewId(view.getId());
        List<ReportViewResponse.ReportViewFieldResponse> fieldResponses = fields.stream()
                .map(this::convertFieldToResponse)
                .collect(Collectors.toList());

        return ReportViewResponse.builder()
                .id(view.getId())
                .name(view.getName())
                .displayName(view.getDisplayName())
                .description(view.getDescription())
                .tableName(view.getTableName())
                .isActive(view.getIsActive())
                .fields(fieldResponses)
                .build();
    }

    private ReportViewResponse.ReportViewFieldResponse convertFieldToResponse(ReportViewField field) {
        return ReportViewResponse.ReportViewFieldResponse.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .displayName(field.getDisplayName())
                .fieldType(field.getFieldType())
                .isFilterable(field.getIsFilterable())
                .isSortable(field.getIsSortable())
                .build();
    }
}
