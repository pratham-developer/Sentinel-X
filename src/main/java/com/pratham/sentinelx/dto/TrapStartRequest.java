package com.pratham.sentinelx.dto;

import lombok.Data;

@Data
public class TrapStartRequest {
    private String userPhone;
    private String scammerPhone;
}
