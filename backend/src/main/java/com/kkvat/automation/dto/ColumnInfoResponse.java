package com.kkvat.automation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfoResponse {
    private String columnName;
    private String dataType;
    private Integer characterMaximumLength;
    private String isNullable;
}
