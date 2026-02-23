package com.kkvat.automation.controller;

import com.kkvat.automation.model.EntityManagement;
import com.kkvat.automation.dto.EntityManagementDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kkvat.automation.security.UserPrincipal;
import com.kkvat.automation.service.EntityManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/entity-management")
@Tag(name = "Entity Management", description = "Manage generator entity configurations")
@SecurityRequirement(name = "Bearer Authentication")
public class EntityManagementController {

    private final EntityManagementService service;
    private final com.kkvat.automation.service.EntityGeneratorService generator;
    private final ObjectMapper objectMapper;

    public EntityManagementController(EntityManagementService service,
                                      com.kkvat.automation.service.EntityGeneratorService generator,
                                      ObjectMapper objectMapper) {
        this.service = service;
        this.generator = generator;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    @Operation(summary = "List entity configs")
    public ResponseEntity<?> list(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            List<EntityManagement> results = service.search(keyword);
            return ResponseEntity.ok(results);
        }
        Page<EntityManagement> page = service.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    @Operation(summary = "Get entity config by id")
    public ResponseEntity<EntityManagement> get(@PathVariable Long id) {
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create entity config")
    public ResponseEntity<?> create(@RequestBody EntityManagementDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            EntityManagement em = objectMapper.convertValue(dto, EntityManagement.class);
            if (dto.getCriteriaFields() != null) em.setCriteriaFields(objectMapper.writeValueAsString(dto.getCriteriaFields()));
            if (dto.getCriteriaValues() != null) em.setCriteriaValues(objectMapper.writeValueAsString(dto.getCriteriaValues()));
            if (dto.getColumns() != null) em.setColumns(objectMapper.writeValueAsString(dto.getColumns()));
            Long userId = principal != null ? principal.getId() : null;
            EntityManagement created = service.create(em, userId);
            // Trigger generator after creation
            String generationResult = null;
            try {
                generationResult = generator.generate(created.getId());
            } catch (Exception genEx) {
                // Optionally log or handle generation failure
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of(
                        "status", "error",
                        "message", "Entity created but generation failed: " + genEx.getMessage(),
                        "entity", created
                    ));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of(
                    "entity", created,
                    "generation", generationResult
                ));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("status","error","message", "Invalid JSON payload: " + e.getMessage()));
        }
    }

    @PostMapping("/generate-from-payload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate backend code and DDL from payload (test only)")
    public ResponseEntity<?> generateFromPayload(@RequestBody EntityManagementDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            EntityManagement em = objectMapper.convertValue(dto, EntityManagement.class);
            if (dto.getCriteriaFields() != null) em.setCriteriaFields(objectMapper.writeValueAsString(dto.getCriteriaFields()));
            if (dto.getCriteriaValues() != null) em.setCriteriaValues(objectMapper.writeValueAsString(dto.getCriteriaValues()));
            if (dto.getColumns() != null) em.setColumns(objectMapper.writeValueAsString(dto.getColumns()));
            String result = generator.generateFromDto(em);
            return ResponseEntity.ok(java.util.Map.of("status","ok","message",result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("status","error","message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update entity config")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EntityManagementDto dto, @AuthenticationPrincipal UserPrincipal principal) {
        try {
            EntityManagement em = objectMapper.convertValue(dto, EntityManagement.class);
            if (dto.getCriteriaFields() != null) em.setCriteriaFields(objectMapper.writeValueAsString(dto.getCriteriaFields()));
            if (dto.getCriteriaValues() != null) em.setCriteriaValues(objectMapper.writeValueAsString(dto.getCriteriaValues()));
            if (dto.getColumns() != null) em.setColumns(objectMapper.writeValueAsString(dto.getColumns()));
            Long userId = principal != null ? principal.getId() : null;
            EntityManagement updated = service.update(id, em, userId);
            return ResponseEntity.ok(updated);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("status","error","message", "Invalid JSON payload: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete entity config")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate backend code and DDL for entity config")
    public ResponseEntity<?> generate(@PathVariable Long id) {
        try {
            String result = generator.generate(id);
            return ResponseEntity.ok(java.util.Map.of("status", "ok", "message", result));
        } catch (Exception e) {
            service.setStatus(id, "GENERATION_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/generated")
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    @Operation(summary = "List generated artifacts")
    public ResponseEntity<?> listGenerated() {
        try {
            return ResponseEntity.ok(generator.listGenerated());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("status","error","message",e.getMessage()));
        }
    }

    @DeleteMapping("/generated/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete generated artifact folder (files only)")
    public ResponseEntity<?> deleteGenerated(@PathVariable String name) {
        try {
            boolean ok = generator.deleteGenerated(name);
            if (!ok) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(java.util.Map.of("status","deleted","name",name));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("status","error","message",e.getMessage()));
        }
    }
}
