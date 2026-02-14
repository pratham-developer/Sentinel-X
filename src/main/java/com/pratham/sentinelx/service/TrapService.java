package com.pratham.sentinelx.service;

import com.pratham.sentinelx.dto.TrapStartRequest;
import com.pratham.sentinelx.dto.VomyraWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrapService {

    private final StringRedisTemplate stringRedisTemplate; // Use String template for simple keys
    private final TranscriptAnalysisService analysisService;
    private final String keyPrefix = "trap_session:";

    // STEP 1: Android starts the trap (User clicks "Answer with AI")
    public void initiateTrap(TrapStartRequest request) {
        try {
            String key = keyPrefix + request.getUserPhone();

            // Save for 10 minutes (plenty of time for call to happen)
            stringRedisTemplate.opsForValue().set(key, request.getScammerPhone(), Duration.ofMinutes(10));

            log.info("🪤 Trap Armed: User {} is engaging Scammer {}", request.getUserPhone(), request.getScammerPhone());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // STEP 2: Webhook arrives from AI Service
    @Async
    public void processVomyraWebhook(VomyraWebhookRequest request) {
        log.info("webhook hit: {}",request);
        String userPhone = request.getFromNumber(); // Provider sees User number
        String key = keyPrefix + userPhone;

        // 1. Retrieve the hidden Scammer Number
        String scammerPhone = stringRedisTemplate.opsForValue().get(key);

        if (scammerPhone == null) {
            log.warn("⚠️ Orphaned Webhook: No trap session found for User {}", userPhone);
            return;
        }

        log.info("🔗 Match Found! Transcript belongs to Scammer {}", scammerPhone);

        // 2. Hand off to the Analysis Service
        analysisService.analyzeAndBlacklist(scammerPhone, request.getTranscript());

        // 3. Clean up session
        stringRedisTemplate.delete(key);
    }
}