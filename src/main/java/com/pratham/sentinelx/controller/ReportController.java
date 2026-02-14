package com.pratham.sentinelx.controller;

import com.pratham.sentinelx.dto.ReportRequest;
import com.pratham.sentinelx.service.CrowdReportService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final CrowdReportService reportService;

    @PostMapping
    public ResponseEntity<String> reportNumber(@RequestBody ReportRequest request) {
        reportService.submitReport(request.getReporter(), request.getScammer(), request.getReason());
        return ResponseEntity.ok("Report Submitted. Thank you for protecting the community.");
    }
}