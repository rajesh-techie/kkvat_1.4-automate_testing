package com.kkvat.automation.generated;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/generated/tbl_users1")
public class Tbl_users1Controller {
    private final Tbl_users1Service service;

    public Tbl_users1Controller(Tbl_users1Service service) { this.service = service; }

    @GetMapping
    public List<Tbl_users1> getAll() { return service.findAll(); }

    @PostMapping
    public Tbl_users1 create(@RequestBody Tbl_users1 e) { return service.save(e); }

    @GetMapping("/{id}")
    public Tbl_users1 get(@PathVariable String id) { return service.findById(id); }

    @PutMapping("/{id}")
    public Tbl_users1 update(@PathVariable String id, @RequestBody Tbl_users1 e) { e.setId(id); return service.save(e); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }
}
