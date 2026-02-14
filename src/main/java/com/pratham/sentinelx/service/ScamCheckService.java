package com.pratham.sentinelx.service;

import com.pratham.sentinelx.dto.CheckCallRequest;
import com.pratham.sentinelx.dto.CheckCallResponse;
import com.pratham.sentinelx.dto.PythonRequest;
import com.pratham.sentinelx.dto.PythonResponse;
import com.pratham.sentinelx.projection.StatsProjection;
import com.pratham.sentinelx.repository.CallLogRepository;
import com.pratham.sentinelx.util.PhoneCheckUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class ScamCheckService {
    private final CallLogRepository callLogRepository;
    private final RestClient pythonClient;
    private final PhoneCheckUtil redisUtil;

    //check method
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackCheck")
    public CheckCallResponse checkIncomingCall(CheckCallRequest request, String traceId) {
        String phone = request.getPhoneNumber();
        log.info("[{}] Checking call from: {}", traceId, phone);

        //redis cache Check (Speed: <5ms)
        if (redisUtil.isBlacklisted(phone)) {
            log.info("[{}] Cache HIT. Phone Detected: {}", traceId, phone);
            return buildResponse("BLOCK", 99, "Previously Confirmed Fraud", traceId);
        }

        //postgres aggregation
        StatsProjection stats = callLogRepository.getCallerStats(phone, LocalDateTime.now().minusHours(1));
        log.info("{}",stats.getFreq());

        //handle new numbers
        if (stats.getFreq() == 0) {
            return buildResponse("ALLOW", 0, "New Number", traceId);
        }

        //ai inference
        PythonRequest pyReq = PythonRequest.builder()
                .phoneNumber(phone)
                .avgDuration(stats.getAvgDur() != null ? stats.getAvgDur() : 0.0)
                .callFrequency(stats.getFreq())
                .uniqueContacts(stats.getUnqContacts())
                .avgCallDistance(stats.getAvgDist() != null ? stats.getAvgDist() : 0.0)
                .circleDiversity(stats.getCircleDiv())
                .build();

        log.info("{}",pyReq);

        PythonResponse aiResponse = pythonClient.post()
                .uri("/internal/predict")
                .body(pyReq)
                .retrieve()
                .body(PythonResponse.class);

        log.info("{}",aiResponse);

        // Final Verdict Logic
        if (aiResponse != null && aiResponse.isAnomaly()) {

            // FIX: Compare against 0.75 (75%), not 75
            if (aiResponse.getConfidence() >= 0.75) {

                // High Confidence (e.g., 0.99) -> BLOCK & Blacklist
                // Cache this scammer for 1 hour to save AI costs next time
                redisUtil.blacklist(phone);

                return buildResponse("BLOCK", (int)(aiResponse.getConfidence() * 100),
                        "High Risk: " + aiResponse.getRiskType(), traceId);
            }

            // Medium Confidence (e.g., 0.60) -> WARN (Don't auto-block)
            return buildResponse("WARN", (int)(aiResponse.getConfidence() * 100),
                    "Suspicious: " + aiResponse.getRiskType(), traceId);
        }

        // Low Confidence / Not Anomaly -> ALLOW
        return buildResponse("ALLOW", 10, "Safe Caller", traceId);
    }

    //fallback method
    public CheckCallResponse fallbackCheck(CheckCallRequest request, String traceId, Throwable t) {
        log.error("[{}] AI Service FAILED. Reason: {}. Defaulting to ALLOW phone: {}.", traceId, t.getMessage(), request.getPhoneNumber());
        return buildResponse("ALLOW", 0, "System Maintenance (Fail Open)", traceId);
    }

    private CheckCallResponse buildResponse(String action, int score, String msg, String traceId) {
        return CheckCallResponse.builder()
                .action(action)
                .riskScore(score)
                .uiMessage(msg)
                .traceId(traceId)
                .build();
    }
}
