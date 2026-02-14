package com.pratham.sentinelx.controller;


import com.pratham.sentinelx.dto.CheckCallRequest;
import com.pratham.sentinelx.dto.CheckCallResponse;
import com.pratham.sentinelx.service.ScamCheckService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/call")
public class CallCheckController {

    private final ScamCheckService scamCheckService;

    @PostMapping("/check")
    public CheckCallResponse checkCall(
            @RequestBody CheckCallRequest request,
            HttpServletRequest servletRequest
    ){
        // Extract Trace ID (from Android) or generate one
        String traceId = servletRequest.getHeader("X-Correlation-ID");
        if (traceId == null) traceId = UUID.randomUUID().toString();

        // Delegate to Service
        return scamCheckService.checkIncomingCall(request, traceId);
    }
}
