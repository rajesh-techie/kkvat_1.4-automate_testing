package com.kkvat.automation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Column(columnDefinition = "json")
    private String criteriaFields;

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
    
    @Column(columnDefinition = "json")
    private String columns;

    @JsonProperty("columns")
    public void setColumnsObject(Object cols) {
        if (cols == null) { this.columns = null; return; }
        if (cols instanceof String) {
            this.columns = (String) cols;
            return;
        }
        try {
            this.columns = new ObjectMapper().writeValueAsString(cols);
        } catch (Exception e) {
            this.columns = cols.toString();
        }
    }
}
