package com.pratham.sentinelx.controller;

import com.pratham.sentinelx.dto.VomyraWebhookRequest;
import com.pratham.sentinelx.service.TrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final TrapService trapService;

    @PostMapping("/vomyra")
    public ResponseEntity<String> handleWebhook(@RequestBody VomyraWebhookRequest request) {
        log.info("📞 Webhook received from AI Provider for User: {}", request.getFromNumber());

        // Fire and forget processing
        trapService.processVomyraWebhook(request);

        return ResponseEntity.ok("Received");
    }
}
