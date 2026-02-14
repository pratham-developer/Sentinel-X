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
    private final CrowdReportService crowdReportService;
    private final RestClient pythonClient;
    private final PhoneCheckUtil redisUtil;

    //check method
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallbackCheck")
    public CheckCallResponse checkIncomingCall(CheckCallRequest request, String traceId) {
        String phone = request.getPhoneNumber();
        log.info("[{}] Checking call from: {}", traceId, phone);

        // 1. Redis Cache Check (Fastest)
        if (redisUtil.isBlacklisted(phone)) {
            log.info("[{}] Cache HIT. Phone Detected: {}", traceId, phone);
            return buildResponse("BLOCK", 99, "Confirmed Fraud (Blacklisted)", traceId);
        }

        // 2. CROWD REPORT CHECK (The "Truecaller" Layer)
        // We check this BEFORE Postgres stats because reports are definitive.
        long reportCount = crowdReportService.getReportCount(phone);

        if (reportCount >= 10) {
            // If 10 people reported it, BLOCK IT immediately. Don't waste AI resources.
            log.warn("🚫 CROWD STRIKE: {} has {} reports. Blocking.", phone, reportCount);
            redisUtil.blacklist(phone); // Add to Redis for next time
            return buildResponse("BLOCK", 95, "Reported by " + reportCount + " Users", traceId);
        }

        if (reportCount >= 3) {
            // 3-9 reports -> We are suspicious. We will continue to AI to see if stats match.
            log.info("⚠️ Suspicious: {} has {} reports.", phone, reportCount);
            // We don't return WARN yet, we let AI decide if it confirms the suspicion.
        }

        // 3. Postgres Aggregation (Network AI)
        StatsProjection stats = callLogRepository.getCallerStats(phone, LocalDateTime.now().minusHours(1));

        // 4. Handle New Numbers
        if (stats.getFreq() == 0) {
            // New number, but has 5 reports? WARN.
            if (reportCount >= 3) {
                return buildResponse("WARN", 60, "New Number (Reported by " + reportCount + ")", traceId);
            }
            return buildResponse("ALLOW", 0, "New Number", traceId);
        }

        // 5. AI Inference
        PythonRequest pyReq = PythonRequest.builder()
                .phoneNumber(phone)
                .avgDuration(stats.getAvgDur() != null ? stats.getAvgDur() : 0.0)
                .callFrequency(stats.getFreq())
                .uniqueContacts(stats.getUnqContacts())
                .avgCallDistance(stats.getAvgDist() != null ? stats.getAvgDist() : 0.0)
                .circleDiversity(stats.getCircleDiv())
                .build();

        PythonResponse aiResponse = pythonClient.post()
                .uri("/internal/predict")
                .body(pyReq)
                .retrieve()
                .body(PythonResponse.class);

        // 6. Final Verdict Logic (Hybrid: AI + Reports)
        if (aiResponse != null && aiResponse.isAnomaly()) {

            // Adjust confidence based on Crowd Reports
            // If AI says 60% confidence, but 5 people reported it -> Bump to 90% (BLOCK)
            double finalConfidence = aiResponse.getConfidence();
            if (reportCount >= 3) {
                finalConfidence += 0.3; // Boost confidence by 30% if community hates him
            }

            if (finalConfidence >= 0.70) {
                redisUtil.blacklist(phone);
                return buildResponse("BLOCK", (int)(Math.min(finalConfidence, 0.99) * 100),
                        "High Risk: " + aiResponse.getRiskType(), traceId);
            }

            return buildResponse("WARN", (int)(finalConfidence * 100),
                    "Suspicious: " + aiResponse.getRiskType(), traceId);
        }

        // AI says Safe, but we have 5 reports? Still WARN.
        if (reportCount >= 3) {
            return buildResponse("WARN", 60, "Reported by Community", traceId);
        }

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
