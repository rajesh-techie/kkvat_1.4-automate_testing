package com.kkvat.automation.controller;

import com.kkvat.automation.dto.ApiResponse;
import com.kkvat.automation.dto.GroupRequest;
import com.kkvat.automation.dto.GroupResponse;
import com.kkvat.automation.security.UserPrincipal;
import com.kkvat.automation.service.GroupService;
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
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Group CRUD operations")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupController {
    
    private final GroupService groupService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get all groups", description = "Retrieve all groups with pagination")
    public ResponseEntity<Page<GroupResponse>> getAllGroups(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(groupService.getAllGroups(pageable));
    }
    
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get all groups (no pagination)", description = "Retrieve all groups without pagination")
    public ResponseEntity<List<GroupResponse>> getAllGroupsList() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get group by ID", description = "Retrieve a specific group by its ID")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }
    
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get group by name", description = "Retrieve a specific group by its name")
    public ResponseEntity<GroupResponse> getGroupByName(@PathVariable String name) {
        return ResponseEntity.ok(groupService.getGroupByName(name));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Create group", description = "Create a new group")
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GroupResponse response = groupService.createGroup(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Update group", description = "Update an existing group")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GroupResponse response = groupService.updateGroup(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete group", description = "Delete a group (Admin only)")
    public ResponseEntity<ApiResponse> deleteGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        groupService.deleteGroup(id, userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Group deleted successfully"));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Search groups", description = "Search groups by name")
    public ResponseEntity<List<GroupResponse>> searchGroups(@RequestParam String keyword) {
        return ResponseEntity.ok(groupService.searchGroups(keyword));
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEST_MANAGER')")
    @Operation(summary = "Get active groups", description = "Retrieve all active groups")
    public ResponseEntity<List<GroupResponse>> getActiveGroups() {
        return ResponseEntity.ok(groupService.getActiveGroups());
    }
}
