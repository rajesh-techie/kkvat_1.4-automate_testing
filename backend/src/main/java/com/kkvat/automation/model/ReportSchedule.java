package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "report_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String scheduleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 0=Sunday, 1=Monday, etc. (for WEEKLY)

    @Column(name = "day_of_month")
    private Integer dayOfMonth; // 1-31 (for MONTHLY, QUARTERLY, ANNUALLY)

    @Column(nullable = false)
    private LocalTime timeOfDay;

    @Column(name = "email_recipients")
    private String emailRecipients; // comma-separated emails

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_executed")
    private LocalDateTime lastExecuted;

    @Column(name = "next_execution")
    private LocalDateTime nextExecution;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public enum Frequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY
    }
}
