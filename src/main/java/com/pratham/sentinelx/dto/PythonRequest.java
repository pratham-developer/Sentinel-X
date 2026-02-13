package com.pratham.sentinelx.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PythonRequest {
    private String phoneNumber;
    private double avgDuration;
    private long callFrequency;
    private long uniqueContacts;
    private double avgCallDistance;
    private long circleDiversity;
}
