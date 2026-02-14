package com.pratham.sentinelx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PythonResponse {

    @JsonProperty("isAnomaly")
    private boolean isAnomaly;

    private double confidence;
    private String riskType;
}
