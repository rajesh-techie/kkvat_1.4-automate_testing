package com.kkvat.automation.generic;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GenericEntityService {

    private final GenericEntityRepository repo;

    public GenericEntityService(GenericEntityRepository repo) {
        this.repo = repo;
    }

    public int create(String table, Map<String, Object> payload) {
        return repo.insert(table, payload);
    }

    public List<Map<String, Object>> list(String table) {
        return repo.findAll(table);
    }

    public Map<String, Object> page(String table, int page, int size, String sort, String dir, String q) {
        return repo.findPage(table, page, size, sort, dir, q);
    }

    public Map<String, Object> getById(String table, String pk, Object id) {
        return repo.findById(table, pk, id);
    }

    public int update(String table, String pk, Object id, Map<String, Object> payload) {
        return repo.update(table, pk, id, payload);
    }

    public int delete(String table, String pk, Object id) {
        return repo.delete(table, pk, id);
    }
}
