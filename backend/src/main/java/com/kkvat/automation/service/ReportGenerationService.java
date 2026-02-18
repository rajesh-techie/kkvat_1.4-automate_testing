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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

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
        
        // Add columns
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("`").append(columns.get(i)).append("`");
        }
        
        sql.append(" FROM `").append(tableName).append("`");

        // Add WHERE clause for filters
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                conditions.add("`" + entry.getKey() + "` = '" + entry.getValue() + "'");
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
                sortExpressions.add("`" + entry.getKey() + "` " + entry.getValue().toString());
            }
            sql.append(String.join(", ", sortExpressions));
        }

        return sql.toString();
    }

    private List<String> parseColumns(String columnsJson) throws Exception {
        return objectMapper.readValue(columnsJson, List.class);
    }

    private Map<String, Object> parseFilters(String filtersJson) throws Exception {
        if (filtersJson == null || filtersJson.isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(filtersJson, Map.class);
    }

    private Map<String, Object> parseSort(String sortJson) throws Exception {
        if (sortJson == null || sortJson.isEmpty()) {
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
}
