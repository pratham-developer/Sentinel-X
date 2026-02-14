package com.pratham.sentinelx.repository;

import com.pratham.sentinelx.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    // Check if this user already reported this number
    boolean existsByReporterNumberAndReportedNumber(String reporter, String reported);

    // Count total reports for a number (The "Score")
    long countByReportedNumber(String reportedNumber);
}