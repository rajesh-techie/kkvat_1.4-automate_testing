package com.kkvat.automation.controller;

import com.kkvat.automation.entity.Role;
import com.kkvat.automation.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Role management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    /**
     * Get all roles
     */
    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve all available roles")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Roles retrieved successfully",
                "data", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get all active roles
     */
    @GetMapping("/active")
    @Operation(summary = "Get active roles", description = "Retrieve all active roles only")
    public ResponseEntity<Map<String, Object>> getActiveRoles() {
        try {
            List<Role> roles = roleService.getAllActiveRoles();
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Active roles retrieved successfully",
                "data", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            return roleService.getRoleById(id)
                .map(role -> ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Role retrieved successfully",
                    "data", role
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", false, "message", "Role not found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get role by name
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name", description = "Retrieve a role by its name")
    public ResponseEntity<Map<String, Object>> getRoleByName(@PathVariable String name) {
        try {
            return roleService.getRoleByName(name)
                .map(role -> ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Role retrieved successfully",
                    "data", role
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", false, "message", "Role not found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Create new role
     */
    @PostMapping
    @Operation(summary = "Create role", description = "Create a new role")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", true,
                "message", "Role created successfully",
                "data", createdRole
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Update role
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update an existing role")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            Role updatedRole = roleService.updateRole(id, role);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role updated successfully",
                "data", updatedRole
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Deactivate role
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate role", description = "Deactivate a role")
    public ResponseEntity<Map<String, Object>> deactivateRole(@PathVariable Long id) {
        try {
            Role deactivatedRole = roleService.deactivateRole(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role deactivated successfully",
                "data", deactivatedRole
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Activate role
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate role", description = "Activate a role")
    public ResponseEntity<Map<String, Object>> activateRole(@PathVariable Long id) {
        try {
            Role activatedRole = roleService.activateRole(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Role activated successfully",
                "data", activatedRole
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
}
