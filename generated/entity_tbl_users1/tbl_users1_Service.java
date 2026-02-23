package com.kkvat.automation.generated;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class Tbl_users1Service {
    // Autowire repository
    private final Tbl_users1Repository repo;

    public Tbl_users1Service(Tbl_users1Repository repo) { this.repo = repo; }

    public List<Tbl_users1> findAll() { return repo.findAll(); }
    public Tbl_users1 save(Tbl_users1 e) { return repo.save(e); }
    public void delete(String id) { repo.deleteById(id); }
    public Tbl_users1 findById(String id) { return repo.findById(id).orElse(null); }
}
