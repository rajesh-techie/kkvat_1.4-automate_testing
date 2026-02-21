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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/{id}/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate backend code and DDL for entity config")
    public ResponseEntity<?> generate(@PathVariable Long id) {
        try {
            // lazy-load generator service to avoid circular dependency in constructor
            com.kkvat.automation.service.EntityGeneratorService generator = org.springframework.web.context.support.SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this.getClass()) == null ? null : null;
            // Instead obtain bean from application context
            org.springframework.context.ApplicationContext ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
            if (ctx == null) {
                // fallback: use repository/service to mark status but indicate unable to generate here
                service.setStatus(id, "GENERATION_PENDING");
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(java.util.Map.of("status", "pending", "message", "Generator will run asynchronously (context not available)"));
            }
            EntityGeneratorInvoker invoker = new EntityGeneratorInvoker(ctx);
            String result = invoker.invoke(id);
            return ResponseEntity.ok(java.util.Map.of("status", "ok", "message", result));
        } catch (Exception e) {
            service.setStatus(id, "GENERATION_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // small helper to bridge to the generator bean without adding it to the constructor (avoids some wiring issues)
    static class EntityGeneratorInvoker {
        private final org.springframework.context.ApplicationContext ctx;
        EntityGeneratorInvoker(org.springframework.context.ApplicationContext ctx) { this.ctx = ctx; }
        String invoke(Long id) throws Exception {
            com.kkvat.automation.service.EntityGeneratorService svc = ctx.getBean(com.kkvat.automation.service.EntityGeneratorService.class);
            return svc.generate(id);
        }
    }
}
