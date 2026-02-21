package com.kkvat.automation.service;

import com.kkvat.automation.model.EntityManagement;
import com.kkvat.automation.repository.EntityManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntityManagementService {

    private final EntityManagementRepository repository;

    public EntityManagementService(EntityManagementRepository repository) {
        this.repository = repository;
    }

    public List<EntityManagement> getAll() {
        return repository.findAll();
    }

    public Optional<EntityManagement> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public EntityManagement create(EntityManagement em, Long createdBy) {
        em.setCreatedAt(LocalDateTime.now());
        em.setCreatedBy(createdBy);
        em.setStatus(em.getStatus() == null ? "ACTIVE" : em.getStatus());
        return repository.save(em);
    }

    @Transactional
    public EntityManagement update(Long id, EntityManagement em, Long updatedBy) {
        EntityManagement existing = repository.findById(id).orElseThrow(() -> new RuntimeException("EntityManagement not found"));
        em.setId(existing.getId());
        em.setCreatedAt(existing.getCreatedAt());
        em.setCreatedBy(existing.getCreatedBy());
        em.setUpdatedAt(LocalDateTime.now());
        return repository.save(em);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public com.kkvat.automation.model.EntityManagement setStatus(Long id, String status) {
        com.kkvat.automation.model.EntityManagement existing = repository.findById(id).orElseThrow(() -> new RuntimeException("EntityManagement not found"));
        existing.setStatus(status);
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        return repository.save(existing);
    }
}
