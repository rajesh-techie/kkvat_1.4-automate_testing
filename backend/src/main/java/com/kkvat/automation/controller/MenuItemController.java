package com.kkvat.automation.controller;

import com.kkvat.automation.dto.MenuItemDTO;
import com.kkvat.automation.entity.MenuItem;
import com.kkvat.automation.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menu-items")
@Tag(name = "Menu Items", description = "Menu item management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MenuItemController {
    
    @Autowired
    private MenuItemService menuItemService;
    
    /**
     * Get all menu items
     */
    @GetMapping
    @Operation(summary = "Get all menu items", description = "Retrieve all available menu items")
    public ResponseEntity<Map<String, Object>> getAllMenuItems() {
        try {
            List<MenuItem> menuItems = menuItemService.getAllActiveMenuItems();
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu items retrieved successfully",
                "data", menuItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get menu item by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID", description = "Retrieve a specific menu item")
    public ResponseEntity<Map<String, Object>> getMenuItemById(@PathVariable Long id) {
        try {
            return menuItemService.getMenuItemById(id)
                .map(menuItem -> ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Menu item retrieved successfully",
                    "data", menuItem
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", false, "message", "Menu item not found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get root-level menu items
     */
    @GetMapping("/root")
    @Operation(summary = "Get root menu items", description = "Retrieve top-level menu items (no parent)")
    public ResponseEntity<Map<String, Object>> getRootMenuItems() {
        try {
            List<MenuItem> rootItems = menuItemService.getRootMenuItems();
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Root menu items retrieved successfully",
                "data", rootItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get child menu items for a parent
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get child menu items", description = "Retrieve child menu items for a specific parent")
    public ResponseEntity<Map<String, Object>> getChildMenuItems(@PathVariable Long parentId) {
        try {
            List<MenuItem> childItems = menuItemService.getChildMenuItems(parentId);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Child menu items retrieved successfully",
                "data", childItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get menu items for a user (by user ID)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user menu items", description = "Retrieve menu items accessible to a specific user based on their role")
    public ResponseEntity<Map<String, Object>> getUserMenuItems(@PathVariable Long userId) {
        try {
            List<MenuItemDTO> userMenus = menuItemService.getMenuItemsByUserId(userId);
            return ResponseEntity.ok(Map.of(
                "status", userMenus,
                "message", "User menu items retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get hierarchical menu items for a user
     */
    @GetMapping("/user/{userId}/hierarchical")
    @Operation(summary = "Get hierarchical menu items", description = "Retrieve menu items for user with parent-child relationships")
    public ResponseEntity<Map<String, Object>> getUserMenuItemsHierarchical(@PathVariable Long userId) {
        try {
            List<MenuItemDTO> userMenus = menuItemService.getMenuItemsHierarchicalByUserId(userId);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "User hierarchical menu items retrieved successfully",
                "data", userMenus
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Create new menu item
     */
    @PostMapping
    @Operation(summary = "Create menu item", description = "Create a new menu item")
    public ResponseEntity<Map<String, Object>> createMenuItem(@RequestBody MenuItem menuItem) {
        try {
            MenuItem createdMenuItem = menuItemService.createMenuItem(menuItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", true,
                "message", "Menu item created successfully",
                "data", createdMenuItem
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Update menu item
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update menu item", description = "Update an existing menu item")
    public ResponseEntity<Map<String, Object>> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        try {
            MenuItem updatedMenuItem = menuItemService.updateMenuItem(id, menuItem);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu item updated successfully",
                "data", updatedMenuItem
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
     * Delete menu item
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item", description = "Delete a menu item")
    public ResponseEntity<Map<String, Object>> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu item deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Deactivate menu item
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate menu item", description = "Deactivate a menu item")
    public ResponseEntity<Map<String, Object>> deactivateMenuItem(@PathVariable Long id) {
        try {
            MenuItem deactivatedMenuItem = menuItemService.deactivateMenuItem(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu item deactivated successfully",
                "data", deactivatedMenuItem
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * Activate menu item
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate menu item", description = "Activate a menu item")
    public ResponseEntity<Map<String, Object>> activateMenuItem(@PathVariable Long id) {
        try {
            MenuItem activatedMenuItem = menuItemService.activateMenuItem(id);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu item activated successfully",
                "data", activatedMenuItem
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Assign a menu item to a role
     */
    @PostMapping("/{menuItemId}/assign/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign menu item to role", description = "Grant access to a menu item for a role")
    public ResponseEntity<Map<String, Object>> assignMenuItemToRole(@PathVariable Long menuItemId, @PathVariable Long roleId) {
        try {
            menuItemService.assignMenuItemToRole(roleId, menuItemId);
            return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Menu item assigned to role successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", false, "message", "Error: " + e.getMessage()));
        }
    }
}
