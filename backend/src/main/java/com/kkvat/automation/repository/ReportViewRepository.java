package com.kkvat.automation.repository;

import com.kkvat.automation.model.ReportView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportViewRepository extends JpaRepository<ReportView, Long> {
    List<ReportView> findByIsActiveTrue();
    Optional<ReportView> findByName(String name);
    List<ReportView> findByNameContainingIgnoreCase(String name);
}
