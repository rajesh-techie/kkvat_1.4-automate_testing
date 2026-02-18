package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ReportSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionType executionType; // MANUAL, SCHEDULED, API

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // PENDING, GENERATING, COMPLETED, FAILED

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "file_path")
    private String filePath; // path to generated CSV file

    @Column(name = "file_size")
    private Long fileSize; // size in bytes

    @Column(name = "row_count")
    private Integer rowCount; // number of rows in report

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by", nullable = false)
    private User executedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ExecutionType {
        MANUAL, SCHEDULED, API
    }

    public enum Status {
        PENDING, GENERATING, COMPLETED, FAILED
    }
}
