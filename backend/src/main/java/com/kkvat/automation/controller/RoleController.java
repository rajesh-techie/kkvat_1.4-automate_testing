package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ApiResponse;
import com.kkvat.automation.dto.RoleRequest;
import com.kkvat.automation.dto.RoleResponse;
import com.kkvat.automation.security.UserPrincipal;
import com.kkvat.automation.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Tag(name = "Role Management", description = "Role CRUD operations")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get all roles", description = "Retrieve all roles with pagination")
    public ResponseEntity<Page<RoleResponse>> getAllRoles(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(roleService.getAllRoles(pageable));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get all roles (no pagination)", description = "Retrieve all roles without pagination")
    public ResponseEntity<List<RoleResponse>> getAllRolesList() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get role by name", description = "Retrieve a specific role by its name")
    public ResponseEntity<RoleResponse> getRoleByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.getRoleByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        RoleResponse response = roleService.createRole(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Update role", description = "Update an existing role")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        RoleResponse response = roleService.updateRole(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete role", description = "Delete a role (Admin only)")
    public ResponseEntity<ApiResponse> deleteRole(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        roleService.deleteRole(id, userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Role deleted successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Search roles", description = "Search roles by name")
    public ResponseEntity<List<RoleResponse>> searchRoles(@RequestParam String keyword) {
        return ResponseEntity.ok(roleService.searchRoles(keyword));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get active roles", description = "Retrieve all active roles")
    public ResponseEntity<List<RoleResponse>> getActiveRoles() {
        return ResponseEntity.ok(roleService.getActiveRoles());
    }
}
