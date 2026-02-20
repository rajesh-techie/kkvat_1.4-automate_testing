package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entity_management")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityManagement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String entityName;
    private String entityTableName;

    private Integer entityColumnsCount;
    private Integer entityColumnStart;
    private String entityColumnNext;

    private Boolean isColumnDropdown;
    private Boolean isColumnCheckbox;
    private Boolean isColumnRadio;
    private Boolean isColumnBlob;
    private String columnType;
    private Integer columnLength;
    private Boolean columnPrimary;
    private Boolean columnIndex;
    private Boolean columnPartOfSearch;
    private Boolean isReferentialIntegrity;
    private Integer entityColumnEnd;

    private Boolean doWeNeedWorkflow;
    private Boolean doWeNeed2LevelWorkflow;
    private Boolean doWeNeed1LevelWorkflow;
    private String workflowStatus;

    private Boolean doWeNeedAuditTable;
    private Boolean doWeNeedArchiveRecords;

    @Lob
    @Column(columnDefinition = "json")
    private String criteriaFields;

    @Lob
    @Column(columnDefinition = "json")
    private String criteriaValues;

    private Boolean doWeNeedCreateView;
    private Integer howManyMonthsMainTable;
    private Integer howManyMonthsArchiveTable;

    @Lob
    private String criteriaToMoveFromMainToArchiveTable;

    @Lob
    private String criteriaToMoveFromArchiveToDeleteTable;

    @Lob
    private String thingsToCreate;

    private String parentMenu;
    private String whichRoleIsEligible;

    private String status;
}
