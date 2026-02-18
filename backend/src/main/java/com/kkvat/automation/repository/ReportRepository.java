package com.kkvat.automation.repository;

import com.kkvat.automation.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByReportType(Report.ReportType reportType);
    
    List<Report> findByCreatedBy(Long createdBy);
    
    List<Report> findByIsPublic(Boolean isPublic);
    
    List<Report> findByIsPublicOrCreatedBy(Boolean isPublic, Long createdBy);
}
