package com.pratham.sentinelx.service;

import com.pratham.sentinelx.dto.NlpAnalysisResponse;
import com.pratham.sentinelx.util.PhoneCheckUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptAnalysisService {

    private final RestClient pythonClient;
    private final PhoneCheckUtil phoneCheckUtil;

    public void analyzeAndBlacklist(String scammerNumber, String transcript) {
        try {
            log.info("🔍 Analyzing Transcript for Scammer: {}", scammerNumber);

            // Ask Python: "Is this text fraud?"
            Map<String, String> payload = Map.of("text", transcript);

            NlpAnalysisResponse result = pythonClient.post()
                    .uri("/internal/analyze-text")
                    .body(payload)
                    .retrieve()
                    .body(NlpAnalysisResponse.class);

            // Process Verdict
            if (result != null && result.isFraudDetected()) {
                log.error("🚨 FRAUD DETECTED! Reason: '{}'. Confidence: {}",
                        result.getReason(), result.getFraudConfidence());

                // Update Redis
                phoneCheckUtil.blacklist(scammerNumber);

                log.info("⛔ ACTION COMPLETE: {} has been blacklisted.", scammerNumber);
            } else {
                log.info("✅ Analysis Passed. No fraud keywords found.");
            }

        } catch (Exception e) {
            log.error("❌ Failed to analyze transcript: {}", e.getMessage());
        }
    }
}