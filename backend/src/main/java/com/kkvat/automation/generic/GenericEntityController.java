package com.kkvat.automation.generic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generic/{table}")
public class GenericEntityController {

    private final GenericEntityService service;

    public GenericEntityController(GenericEntityService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable String table, @RequestBody Map<String, Object> payload, @RequestParam(name = "pk", required = false, defaultValue = "id") String pk) {
        int updated = service.create(table, payload);
        return ResponseEntity.ok(Map.of("rows", updated));
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable String table,
                                   @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                   @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                   @RequestParam(name = "sort", required = false) String sort,
                                   @RequestParam(name = "dir", required = false, defaultValue = "ASC") String dir,
                                   @RequestParam(name = "q", required = false) String q) {
        Map<String, Object> result = service.page(table, page, size, sort, dir, q);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String table, @PathVariable String id, @RequestParam(name = "pk", required = false, defaultValue = "id") String pk) {
        Map<String, Object> row = service.getById(table, pk, id);
        if (row == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(row);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String table, @PathVariable String id, @RequestBody Map<String, Object> payload, @RequestParam(name = "pk", required = false, defaultValue = "id") String pk) {
        int updated = service.update(table, pk, id, payload);
        return ResponseEntity.ok(Map.of("rows", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String table, @PathVariable String id, @RequestParam(name = "pk", required = false, defaultValue = "id") String pk) {
        int deleted = service.delete(table, pk, id);
        return ResponseEntity.ok(Map.of("rows", deleted));
    }
}
