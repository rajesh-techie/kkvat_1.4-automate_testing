package com.kkvat.automation.generated;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/generated/tbl_users5")
public class Tbl_users5Controller {
    private final Tbl_users5Service service;

    public Tbl_users5Controller(Tbl_users5Service service) { this.service = service; }

    @GetMapping
    public List<Tbl_users5> getAll() { return service.findAll(); }

    @PostMapping
    public Tbl_users5 create(@RequestBody Tbl_users5 e) { return service.save(e); }

    @GetMapping("/{id}")
    public Tbl_users5 get(@PathVariable String id) { return service.findById(id); }

    @PutMapping("/{id}")
    public Tbl_users5 update(@PathVariable String id, @RequestBody Tbl_users5 e) { e.setId(id); return service.save(e); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { service.delete(id); }
}
