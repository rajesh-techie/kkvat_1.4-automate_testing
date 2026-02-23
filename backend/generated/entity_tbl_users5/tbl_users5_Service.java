package com.kkvat.automation.generated;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class Tbl_users5Service {
    // Autowire repository
    private final Tbl_users5Repository repo;

    public Tbl_users5Service(Tbl_users5Repository repo) { this.repo = repo; }

    public List<Tbl_users5> findAll() { return repo.findAll(); }
    public Tbl_users5 save(Tbl_users5 e) { return repo.save(e); }
    public void delete(String id) { repo.deleteById(id); }
    public Tbl_users5 findById(String id) { return repo.findById(id).orElse(null); }
}
