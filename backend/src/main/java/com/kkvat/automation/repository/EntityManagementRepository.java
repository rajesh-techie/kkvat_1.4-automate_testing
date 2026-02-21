package com.kkvat.automation.repository;

import com.kkvat.automation.model.EntityManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntityManagementRepository extends JpaRepository<EntityManagement, Long> {
	List<EntityManagement> findByEntityNameContainingIgnoreCaseOrEntityTableNameContainingIgnoreCase(String name, String tableName);
}
