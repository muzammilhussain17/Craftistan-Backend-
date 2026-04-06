package com.craftistan.report.repository;

import com.craftistan.report.entity.Report;
import com.craftistan.report.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByReporterId(String reporterId, Pageable pageable);

    long countByStatus(ReportStatus status);
}
