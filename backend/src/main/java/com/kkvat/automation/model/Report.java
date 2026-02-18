package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "view_id", nullable = false)
    private ReportView view;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_columns", nullable = false, columnDefinition = "JSON")
    private String selectedColumns; // JSON array: ["col1", "col2", "col3"]
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter_conditions", columnDefinition = "JSON")
    private String filterConditions; // JSON with filter rules
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sort_config", columnDefinition = "JSON")
    private String sortConfig; // JSON with sort configuration
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;
    
    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    public enum ReportType {
        EXECUTION,
        USER_ACTIVITY,
        CUSTOM
    }
}
