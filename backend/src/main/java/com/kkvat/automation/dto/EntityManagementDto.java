package com.kkvat.automation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntityManagementDto {
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

    private JsonNode criteriaFields;
    private JsonNode criteriaValues;

    private Boolean doWeNeedCreateView;
    private Integer howManyMonthsMainTable;
    private Integer howManyMonthsArchiveTable;

    private String criteriaToMoveFromMainToArchiveTable;
    private String criteriaToMoveFromArchiveToDeleteTable;

    private String thingsToCreate;

    private String parentMenu;
    private String whichRoleIsEligible;

    private String status;

    private JsonNode columns;
}
