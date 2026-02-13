package com.pratham.sentinelx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PythonResponse {
    private boolean isAnomaly;
    private double confidence;
    private String riskType;
}
