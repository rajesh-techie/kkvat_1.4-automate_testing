package com.kkvat.automation.dto;

import com.kkvat.automation.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    public static RoleResponse from(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isActive(role.getIsActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }
}
