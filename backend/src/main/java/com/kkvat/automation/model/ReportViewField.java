package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_view_fields", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"view_id", "field_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportViewField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "view_id", nullable = false)
    private ReportView view;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String fieldType; // STRING, NUMBER, DATE, BOOLEAN, etc.

    @Column(nullable = false)
    private Boolean isFilterable = true;

    @Column(nullable = false)
    private Boolean isSortable = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
