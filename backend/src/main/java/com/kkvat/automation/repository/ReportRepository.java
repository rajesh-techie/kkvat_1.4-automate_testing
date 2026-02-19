package com.kkvat.automation.repository;

import com.kkvat.automation.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByReportType(Report.ReportType reportType);
    
    List<Report> findByCreatedById(Long createdById);
    
    List<Report> findByIsPublic(Boolean isPublic);
    
    List<Report> findByIsPublicOrCreatedById(Boolean isPublic, Long createdById);
    
    Page<Report> findByCreatedByIdOrIsPublicTrue(Long createdById, Pageable pageable);
    
    List<Report> findByViewId(Long viewId);
    
    Page<Report> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Report findTopByOrderByIdDesc();
}
