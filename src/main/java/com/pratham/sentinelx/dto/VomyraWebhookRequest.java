package com.pratham.sentinelx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VomyraWebhookRequest {
    @JsonProperty("call_id")
    private String callId;

    @JsonProperty("from_number")
    private String fromNumber; // This is the USER's number (the bridge key)

    private String transcript;
    private Integer duration;
}