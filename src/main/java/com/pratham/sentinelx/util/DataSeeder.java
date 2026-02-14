package com.pratham.sentinelx.util;

import com.pratham.sentinelx.entity.CallLog;
import com.pratham.sentinelx.repository.CallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CallLogRepository repository;
    private final Random random = new Random();

    @Value("${scammer.phone}")
    private String SCAMMER_PHONE;

    @Value("${delivery.boy.phone}")
    private String DELIVERY_PHONE;

    @Value("${tele.marketer.phone}")
    private String TELEMARKETER_PHONE;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) return;

        List<CallLog> logs = new ArrayList<>();

        // ─────────────────────────────────────────────────────────────
        // SCAMMER: Digital Arrest Bot
        // Target: 3/3 primary FRAUD + dist FRAUD + circles FRAUD
        // → DIGITAL_ARREST_BOT (confidence 0.97)
        // ─────────────────────────────────────────────────────────────
        String[] scammerCircles = {"DELHI", "MUMBAI", "CHENNAI", "KOLKATA", "HYDERABAD", "PUNE", "LUCKNOW"};
        for (int i = 0; i < 150; i++) {
            logs.add(createLog(
                    SCAMMER_PHONE,
                    scammerCircles[i % scammerCircles.length],  // Rotate through all 7 circles
                    1500 + random.nextInt(1001),                // 1500-2500 km (avg ~2000, FRAUD >1000)
                    5 + random.nextInt(8)                       // 5-12 seconds (avg ~8.5s, FRAUD <15)
            ));
        }

        // ─────────────────────────────────────────────────────────────
        // DELIVERY BOY: Safe user (Swiggy/Zomato driver)
        // Target: 3/3 primary SAFE + all secondary SAFE
        // → SAFE (confidence 0.92)
        // ─────────────────────────────────────────────────────────────
        for (int i = 0; i < 25; i++) {
            logs.add(createLog(
                    DELIVERY_PHONE,
                    "BANGALORE",                                // 1 circle (SAFE ≤3)
                    3 + random.nextInt(13),                     // 3-15 km (avg ~9km, SAFE <200)
                    40 + random.nextInt(41)                     // 40-80 seconds (avg ~60s, SAFE ≥30)
            ));
        }

        // ─────────────────────────────────────────────────────────────
        // TELEMARKETER: Suspicious pattern (Bajaj Finance / Loan calls)
        // Target: 0 FRAUD primary, 2 UNCERTAIN (dur+freq), 1 SAFE (contacts)
        //         + secondary bad (dist UNCERTAIN, circles UNCERTAIN)
        // → SUSPICIOUS_PATTERN (confidence 0.65)
        // ─────────────────────────────────────────────────────────────
        String[] teleCircles = {"PUNE", "MUMBAI", "DELHI", "CHENNAI"};
        for (int i = 0; i < 45; i++) {
            logs.add(createLog(
                    TELEMARKETER_PHONE,
                    teleCircles[i % teleCircles.length],        // 4 circles (UNCERTAIN >3 and <6)
                    300 + random.nextInt(301),                   // 300-600 km (avg ~450km, UNCERTAIN 200-1000)
                    18 + random.nextInt(8)                       // 18-25 seconds (avg ~21.5s, UNCERTAIN 15-30)
            ));
        }

        repository.saveAll(logs);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("  DATABASE SEEDED — AI Model Expected Classifications:");
        System.out.println("  SCAMMER      → DIGITAL_ARREST_BOT  (isAnomaly=true)");
        System.out.println("  DELIVERY     → SAFE                (isAnomaly=false)");
        System.out.println("  TELEMARKETER → SUSPICIOUS_PATTERN  (isAnomaly=true)");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }

    private CallLog createLog(String caller, String circle, int dist, int dur) {
        CallLog log = new CallLog();
        log.setCallerNumber(caller);
        log.setReceiverCircle(circle);
        log.setReceiverNumber("+91" + (6000000000L + random.nextInt(900000000)));
        log.setDistanceKm(dist);
        log.setDuration(dur);
        log.setTimestamp(LocalDateTime.now().minusMinutes(random.nextInt(1440))); // Spread across last 24h
        return log;
    }
}