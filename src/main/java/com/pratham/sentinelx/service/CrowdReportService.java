package com.pratham.sentinelx.service;


import com.pratham.sentinelx.entity.UserReport;
import com.pratham.sentinelx.repository.UserReportRepository;
import com.pratham.sentinelx.util.PhoneCheckUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrowdReportService {

    private final UserReportRepository reportRepository;
    private final PhoneCheckUtil phoneCheckUtil;

    // The Threshold
    private static final int BLOCK_THRESHOLD = 10;

    public void submitReport(String reporter, String scammer, String reason) {
        // 1. Prevent duplicate reports (One vote per person)
        if (reportRepository.existsByReporterNumberAndReportedNumber(reporter, scammer)) {
            log.warn("⚠️ User {} already reported {}", reporter, scammer);
            return;
        }

        // 2. Save the Report
        UserReport report = new UserReport(reporter, scammer, reason);
        reportRepository.save(report);

        // 3. Check the "Crowd Score"
        long totalReports = reportRepository.countByReportedNumber(scammer);
        log.info("📢 Number {} now has {} reports.", scammer, totalReports);

        // 4. THE THRESHOLD CHECK
        if (totalReports >= BLOCK_THRESHOLD) {
            log.error("🚫 CROWD STRIKE: {} crossed {} reports. BLACKLISTING!", scammer, BLOCK_THRESHOLD);
            phoneCheckUtil.blacklist(scammer);
        }
    }

    public long getReportCount(String number) {
        return reportRepository.countByReportedNumber(number);
    }
}
