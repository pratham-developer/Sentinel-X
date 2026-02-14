package com.pratham.sentinelx.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NlpAnalysisResponse {
    private boolean fraudDetected;
    private double fraudConfidence;
    private String reason;
}