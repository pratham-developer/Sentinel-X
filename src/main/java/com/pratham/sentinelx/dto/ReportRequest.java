package com.pratham.sentinelx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
    private String reporter; // "+91888..."
    private String scammer;  // "+91999..."
    private String reason;
}
