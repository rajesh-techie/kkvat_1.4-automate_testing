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

import com.kkvat.automation.dto.ColumnInfoResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportViewService {
    private final ReportViewRepository reportViewRepository;
    private final ReportViewFieldRepository reportViewFieldRepository;
    @PersistenceContext
    private EntityManager entityManager;

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

    public List<ColumnInfoResponse> getColumnsForView(Long viewId) {
        return getColumnsForView(viewId, "kkvat_automation");
    }

    public List<ColumnInfoResponse> getColumnsForView(Long viewId, String schema) {
        ReportView view = reportViewRepository.findById(viewId)
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with id: " + viewId));

        String tableName = view.getTableName();

        String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = :schema AND TABLE_NAME = :tableName " +
                "ORDER BY ORDINAL_POSITION";

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("schema", schema)
                .setParameter("tableName", tableName)
                .getResultList();

        List<ColumnInfoResponse> cols = new ArrayList<>();
        for (Object[] r : rows) {
            String columnName = r[0] != null ? r[0].toString() : null;
            String dataType = r[1] != null ? r[1].toString() : null;
            Integer charMax = null;
            if (r[2] != null) {
                if (r[2] instanceof BigInteger) {
                    charMax = ((BigInteger) r[2]).intValue();
                } else if (r[2] instanceof Number) {
                    charMax = ((Number) r[2]).intValue();
                } else {
                    try { charMax = Integer.parseInt(r[2].toString()); } catch (Exception ignored) {}
                }
            }
            String isNullable = r[3] != null ? r[3].toString() : null;
            cols.add(new ColumnInfoResponse(columnName, dataType, charMax, isNullable));
        }

        return cols;
    }

    public List<ColumnInfoResponse> getColumnsForViewByName(String name) {
        return getColumnsForViewByName(name, "kkvat_automation");
    }

    public List<ColumnInfoResponse> getColumnsForViewByName(String name, String schema) {
        ReportView view = reportViewRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Report view not found with name: " + name));
        return getColumnsForView(view.getId(), schema);
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
