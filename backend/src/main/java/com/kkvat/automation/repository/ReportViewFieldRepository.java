package com.kkvat.automation.repository;

import com.kkvat.automation.model.ReportViewField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportViewFieldRepository extends JpaRepository<ReportViewField, Long> {
    List<ReportViewField> findByViewId(Long viewId);
    List<ReportViewField> findByViewIdAndIsFilterableTrue(Long viewId);
    List<ReportViewField> findByViewIdAndIsSortableTrue(Long viewId);
}
