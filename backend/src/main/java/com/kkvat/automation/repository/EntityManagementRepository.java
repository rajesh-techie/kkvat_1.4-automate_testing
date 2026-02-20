package com.kkvat.automation.repository;

import com.kkvat.automation.model.EntityManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityManagementRepository extends JpaRepository<EntityManagement, Long> {
}
