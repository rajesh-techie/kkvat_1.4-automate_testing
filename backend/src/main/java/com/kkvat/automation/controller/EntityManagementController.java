package com.kkvat.automation.controller;

import com.kkvat.automation.model.EntityManagement;
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

@RestController
@RequestMapping("/api/entity-management")
@Tag(name = "Entity Management", description = "Manage generator entity configurations")
@SecurityRequirement(name = "Bearer Authentication")
public class EntityManagementController {

    private final EntityManagementService service;

    public EntityManagementController(EntityManagementService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEST_MANAGER')")
    @Operation(summary = "List entity configs")
    public ResponseEntity<List<EntityManagement>> list() {
        return ResponseEntity.ok(service.getAll());
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
    public ResponseEntity<EntityManagement> create(@RequestBody EntityManagement em, @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        EntityManagement created = service.create(em, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update entity config")
    public ResponseEntity<EntityManagement> update(@PathVariable Long id, @RequestBody EntityManagement em, @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        EntityManagement updated = service.update(id, em, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete entity config")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
