package com.pratham.sentinelx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckCallResponse {
    private String action;      // BLOCK, WARN, ALLOW
    private int riskScore;      // 0-100
    private String uiMessage;   // "High Risk: Digital Arrest Pattern"
    private String traceId;     // For debugging
}
