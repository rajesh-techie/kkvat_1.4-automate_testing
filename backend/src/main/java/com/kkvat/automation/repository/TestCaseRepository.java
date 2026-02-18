package com.kkvat.automation.repository;

import com.kkvat.automation.model.Group;
import com.kkvat.automation.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    List<TestCase> findByStatus(TestCase.Status status);
    
    List<TestCase> findByCreatedBy(Long createdBy);
    
    List<TestCase> findByGroupId(Long groupId);
    
    List<TestCase> findByGroup(Group group);
    
    List<TestCase> findByNameContainingOrDescriptionContaining(String name, String description);
    
    @Query("SELECT tc FROM TestCase tc WHERE tc.name LIKE %:keyword% OR tc.description LIKE %:keyword%")
    List<TestCase> searchByKeyword(String keyword);
    
    List<TestCase> findByTagsContaining(String tag);
}
