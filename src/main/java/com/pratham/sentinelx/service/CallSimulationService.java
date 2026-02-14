package com.pratham.sentinelx.service;

import com.pratham.sentinelx.entity.CallLog;
import com.pratham.sentinelx.repository.CallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallSimulationService {
    private final CallLogRepository repository;
    private final Random random = new Random();

    @Value("${delivery.boy.phone}")
    private String DELIVERY_BOY;

    @Value("${tele.marketer.phone}")
    private String TELEMARKETER;

    @Value("${scammer.phone}")
    private String SCAMMER;

    // SCAMMER circles (7 unique → circleDiversity FRAUD ≥6)
    private final String[] SCAMMER_CIRCLES = {
            "DELHI", "MUMBAI", "CHENNAI", "KOLKATA", "HYDERABAD", "PUNE", "LUCKNOW"
    };

    // TELEMARKETER circles (4 unique → circleDiversity UNCERTAIN >3 and <6)
    private final String[] TELE_CIRCLES = {"PUNE", "MUMBAI", "DELHI", "CHENNAI"};

    // DELIVERY circle (1 unique → circleDiversity SAFE ≤3)
    private final String DELIVERY_CIRCLE = "BANGALORE";

    /**
     * Runs every 5 seconds.
     * Weighted random: 75% scammer, 10% delivery, 15% telemarketer.
     * This keeps scammer frequency high while delivery stays low.
     */
    @Scheduled(fixedRate = 5000*12)
    public void simulateTraffic() {
        int roll = random.nextInt(100);

        if (roll < 75) {
            // ── SCAMMER: short calls, far distance, rotating circles ──
            // avgDuration target: <15s (FRAUD zone)
            // avgCallDistance target: >1000km (FRAUD zone)
            String circle = SCAMMER_CIRCLES[random.nextInt(SCAMMER_CIRCLES.length)];
            int dist = 1500 + random.nextInt(1001);     // 1500-2500 km
            int dur = 5 + random.nextInt(8);             // 5-12 seconds
            saveCall(SCAMMER, circle, dist, dur);
        }

        else if (roll < 85) {
            // ── DELIVERY BOY: normal calls, local, single circle ──
            // avgDuration target: ≥30s (SAFE zone)
            // avgCallDistance target: <200km (SAFE zone)
            int dist = 3 + random.nextInt(13);           // 3-15 km
            int dur = 40 + random.nextInt(41);           // 40-80 seconds
            saveCall(DELIVERY_BOY, DELIVERY_CIRCLE, dist, dur);
        }

        else {
            // ── TELEMARKETER: moderate-short calls, mid distance ──
            // avgDuration target: 15-30s (UNCERTAIN zone)
            // avgCallDistance target: 200-1000km (UNCERTAIN zone)
            String circle = TELE_CIRCLES[random.nextInt(TELE_CIRCLES.length)];
            int dist = 300 + random.nextInt(301);        // 300-600 km
            int dur = 18 + random.nextInt(8);            // 18-25 seconds
            saveCall(TELEMARKETER, circle, dist, dur);
        }
    }

    private void saveCall(String caller, String circle, int dist, int dur) {
        CallLog log = new CallLog();
        log.setCallerNumber(caller);
        log.setReceiverCircle(circle);
        log.setReceiverNumber(generateRandomNumber());
        log.setDistanceKm(dist);
        log.setDuration(dur);
        log.setTimestamp(LocalDateTime.now());
        repository.save(log);
    }

    private String generateRandomNumber() {
        return "+91" + (6000000000L + random.nextInt(900000000));
    }
}