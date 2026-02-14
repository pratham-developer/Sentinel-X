package com.pratham.sentinelx.controller;

import com.pratham.sentinelx.dto.TrapStartRequest;
import com.pratham.sentinelx.service.TrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trap")
@RequiredArgsConstructor
public class TrapController {

    private final TrapService trapService;

    @PostMapping("/start")
    public ResponseEntity<String> startTrap(@RequestBody TrapStartRequest request) {
        trapService.initiateTrap(request);
        return ResponseEntity.ok("Trap Session Armed");
    }
}
