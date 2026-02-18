package com.kkvat.automation.repository;

import com.kkvat.automation.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByName(String name);
    
    Boolean existsByName(String name);
    
    List<Group> findByIsActive(Boolean isActive);
    
    List<Group> findByNameContaining(String keyword);
    
    List<Group> findByCreatedBy(Long createdBy);
}
