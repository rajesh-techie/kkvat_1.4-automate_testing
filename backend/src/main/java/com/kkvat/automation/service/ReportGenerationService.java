package com.kkvat.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkvat.automation.model.Report;
import com.kkvat.automation.model.User;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.LinkedHashMap;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final com.kkvat.automation.repository.ReportViewFieldRepository reportViewFieldRepository;

    @Value("${report.output.directory:./reports}")
    private String reportOutputDirectory;

    @Data
    @Builder
    public static class ReportResult {
        private String filePath;
        private Long fileSize;
        private Integer rowCount;
    }

    public ReportResult generateCsvReport(Report report, User user) throws Exception {
        // Ensure output directory exists
        Files.createDirectories(Paths.get(reportOutputDirectory));

        // Parse report configuration
        List<String> columns = parseColumns(report.getSelectedColumns());
        Map<String, Object> filters = parseFilters(report.getFilterConditions());
        Map<String, Object> sortConfig = parseSort(report.getSortConfig());
        String tableName = report.getView().getTableName();

        // Build and execute SQL query
        String sql = buildSelectQuery(tableName, columns, filters, sortConfig);
        log.info("Executing report SQL: {}", sql);

        // Generate CSV file
        String fileName = generateFileName(report, user);
        String filePath = reportOutputDirectory + File.separator + fileName;

        int rowCount = 0;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             FileWriter csvWriter = new FileWriter(filePath)) {

            ResultSetMetaData metadata = rs.getMetaData();

            // Write header
            StringBuilder header = new StringBuilder();
            for (String col : columns) {
                if (header.length() > 0) header.append(",");
                header.append("\"").append(escapeForCsv(col)).append("\"");
            }
            csvWriter.write(header.toString());
            csvWriter.write("\n");

            // Write data rows
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (String col : columns) {
                    if (row.length() > 0) row.append(",");
                    Object value = rs.getObject(col);
                    row.append("\"").append(escapeForCsv(value != null ? value.toString() : "")).append("\"");
                }
                csvWriter.write(row.toString());
                csvWriter.write("\n");
                rowCount++;
            }
        }

        File csvFile = new File(filePath);
        long fileSize = csvFile.length();

        return ReportResult.builder()
                .filePath(filePath)
                .fileSize(fileSize)
                .rowCount(rowCount)
                .build();
    }

    private String buildSelectQuery(String tableName, List<String> columns, Map<String, Object> filters, Map<String, Object> sortConfig) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // Add columns (no identifier quoting)
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i));
        }

        sql.append(" FROM ").append(tableName);

        // Add WHERE clause for filters
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                conditions.add(entry.getKey() + " = '" + entry.getValue() + "'");
            }
            sql.append(String.join(" AND ", conditions));
        }

        // Add ORDER BY clause for sorting
        if (sortConfig != null && !sortConfig.isEmpty()) {
            sql.append(" ORDER BY ");
            List<String> sortExpressions = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> sorts = (Map<String, Object>) sortConfig;
            
            for (Map.Entry<String, Object> entry : sorts.entrySet()) {
                sortExpressions.add(entry.getKey() + " " + entry.getValue().toString());
            }
            sql.append(String.join(", ", sortExpressions));
        }

        return sql.toString();
    }

    private String buildSelectQuery(String tableName, List<String> columns, Map<String, Object> filters, Map<String, Object> sortConfig, Map<String, String> fieldTypeMap) {
        StringBuilder sql = new StringBuilder("SELECT ");

        // Add columns (no identifier quoting)
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(columns.get(i));
        }

        sql.append(" FROM ").append(tableName);

        // Add WHERE clause for filters
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String fname = entry.getKey();
                Object val = entry.getValue();
                String ftype = fieldTypeMap.getOrDefault(fname, "STRING");
                if (val == null) {
                    conditions.add(fname + " IS NULL");
                } else if ("STRING".equalsIgnoreCase(ftype) || val instanceof String) {
                    // use LIKE for string filters
                    String escaped = String.valueOf(val).replace("%", "\\%").replace("'", "\\'");
                    conditions.add(fname + " LIKE '%" + escaped + "%'");
                } else {
                    // default to equality for non-strings
                    conditions.add(fname + " = '" + String.valueOf(val) + "'");
                }
            }
            sql.append(String.join(" AND ", conditions));
        }

        // Add ORDER BY clause for sorting
        if (sortConfig != null && !sortConfig.isEmpty()) {
            sql.append(" ORDER BY ");
            List<String> sortExpressions = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> sorts = (Map<String, Object>) sortConfig;
            for (Map.Entry<String, Object> entry : sorts.entrySet()) {
                sortExpressions.add(entry.getKey() + " " + entry.getValue().toString());
            }
            sql.append(String.join(", ", sortExpressions));
        }

        return sql.toString();
    }

    public List<String> parseColumns(String columnsJson) throws Exception {
        return objectMapper.readValue(columnsJson, List.class);
    }

    public Map<String, Object> parseFilters(String filtersJson) throws Exception {
        if (filtersJson == null || filtersJson.isEmpty()) {
            return new HashMap<>();
        }

        String trimmed = filtersJson.trim();
        // Accept either an object (e.g. {"email":"admin"}) or an array (e.g. ["email"]).
        if (trimmed.startsWith("{")) {
            return objectMapper.readValue(filtersJson, Map.class);
        } else if (trimmed.startsWith("[")) {
            // Array of field names -> convert to map with null values
            List<String> keys = objectMapper.readValue(filtersJson, List.class);
            Map<String, Object> result = new LinkedHashMap<>();
            for (String k : keys) {
                result.put(k, null);
            }
            return result;
        } else {
            // Fallback: attempt to parse as a map
            return objectMapper.readValue(filtersJson, Map.class);
        }
    }

    public Map<String, Object> parseSort(String sortJson) throws Exception {
        if (sortJson == null || sortJson.isEmpty()) {
            return new HashMap<>();
        }
        String trimmed = sortJson.trim();
        // If sort config accidentally stored as an array, treat as no-sort
        if (trimmed.startsWith("[")) {
            return new HashMap<>();
        }
        return objectMapper.readValue(sortJson, Map.class);
    }

    private String generateFileName(Report report, User user) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return String.format("Report_%s_%s_%s.csv", 
                report.getId(), 
                now.format(formatter),
                user.getId());
    }

    private String escapeForCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public List<Map<String, Object>> executeQuery(Report report, Map<String, Object> overrideFilters) throws Exception {
        List<String> logicalColumns = parseColumns(report.getSelectedColumns());
        Map<String, Object> filters = parseFilters(report.getFilterConditions());
        if (overrideFilters != null) {
            filters.putAll(overrideFilters);
        }
        Map<String, Object> sortConfig = parseSort(report.getSortConfig());
        String tableName = report.getView().getTableName();

        // Build mapping information if view is present so we can use type-aware filtering
        Long viewId = report.getView() != null ? report.getView().getId() : null;
        java.util.Map<String, String> logicalToPhysical = new java.util.HashMap<>();
        java.util.Map<String, String> fieldTypeMap = new java.util.HashMap<>();
        if (viewId != null) {
            List<com.kkvat.automation.model.ReportViewField> fields = reportViewFieldRepository.findByViewId(viewId);
            for (com.kkvat.automation.model.ReportViewField f : fields) {
                if (f.getDisplayName() != null) logicalToPhysical.put(f.getDisplayName().toLowerCase(), f.getFieldName());
                if (f.getFieldName() != null) logicalToPhysical.put(f.getFieldName().toLowerCase(), f.getFieldName());
                fieldTypeMap.put(f.getFieldName(), f.getFieldType());
            }
        }

        // Map logical columns to physical columns for the query
        List<String> physicalColumns = new ArrayList<>();
        for (String col : logicalColumns) {
            String mapped = logicalToPhysical.getOrDefault(col.toLowerCase(), col);
            physicalColumns.add(mapped);
        }

        // Map filter keys to physical names
        Map<String, Object> physicalFilters = new java.util.HashMap<>();
        if (filters != null) {
            for (Map.Entry<String, Object> e : filters.entrySet()) {
                String key = e.getKey();
                String mappedKey = logicalToPhysical.getOrDefault(key.toLowerCase(), key);
                physicalFilters.put(mappedKey, e.getValue());
            }
        }

        String sql = buildSelectQuery(tableName, physicalColumns, physicalFilters, sortConfig != null ? sortConfig : new HashMap<>(), fieldTypeMap);
        log.info("Executing report SQL (direct): {}", sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metadata = rs.getMetaData();
            int colCount = metadata.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colLabel = metadata.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(colLabel, value);
                }
                rows.add(row);
            }
        }

        return rows;
    }

    /**
     * Execute a query using explicit parameters (table name, columns, filters, sortConfig).
     */
    public List<Map<String, Object>> executeRawQuery(Long viewId, String tableName, List<String> columns, Map<String, Object> filters, Map<String, Object> sortConfig) throws Exception {
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("Columns must be provided");
        if (tableName == null || tableName.isEmpty()) throw new IllegalArgumentException("tableName must be provided");

        // If viewId provided, map requested logical column names and filter keys to actual field names
        java.util.Map<String, String> logicalToPhysical = new java.util.HashMap<>();
        java.util.Map<String, String> fieldTypeMap = new java.util.HashMap<>();
        if (viewId != null) {
            List<com.kkvat.automation.model.ReportViewField> fields = reportViewFieldRepository.findByViewId(viewId);
            for (com.kkvat.automation.model.ReportViewField f : fields) {
                if (f.getDisplayName() != null) logicalToPhysical.put(f.getDisplayName().toLowerCase(), f.getFieldName());
                if (f.getFieldName() != null) logicalToPhysical.put(f.getFieldName().toLowerCase(), f.getFieldName());
                fieldTypeMap.put(f.getFieldName(), f.getFieldType());
            }
        }

        // Map columns
        List<String> physicalColumns = new ArrayList<>();
        for (String col : columns) {
            String mapped = logicalToPhysical.getOrDefault(col.toLowerCase(), col);
            physicalColumns.add(mapped);
        }

        // Map filters: convert keys to physical names
        Map<String, Object> physicalFilters = new java.util.HashMap<>();
        if (filters != null) {
            for (Map.Entry<String, Object> e : filters.entrySet()) {
                String key = e.getKey();
                String mappedKey = logicalToPhysical.getOrDefault(key.toLowerCase(), key);
                physicalFilters.put(mappedKey, e.getValue());
            }
        }

        String sql = buildSelectQuery(tableName, physicalColumns, physicalFilters, sortConfig != null ? sortConfig : new HashMap<>(), fieldTypeMap);
        log.info("Executing raw report SQL: {}", sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metadata = rs.getMetaData();
            int colCount = metadata.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colLabel = metadata.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(colLabel, value);
                }
                rows.add(row);
            }
        }

        return rows;
    }
}
