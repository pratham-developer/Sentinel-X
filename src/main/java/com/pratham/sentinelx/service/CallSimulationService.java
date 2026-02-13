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

    // The 3 Actors
    @Value("${delivery.boy.phone}")
    private String DELIVERY_BOY; // GREEN: Swiggy/Zomato

    @Value("${tele.marketer.phone}")
    private String TELEMARKETER; // YELLOW: Bajaj Finance / Loans

    @Value("${scammer.phone}")
    private String SCAMMER; // RED: Digital Arrest Bot

    // Circle Pools
    private final String[] SCAMMER_CIRCLES = {"DELHI", "MUMBAI", "CHENNAI", "KOLKATA"}; // Div: 4
    private final String[] TELE_CIRCLES = {"PUNE", "MUMBAI"}; // Div: 2
    private final String DELIVERY_CIRCLE = "BANGALORE"; // Div: 1

    // The "Map"
    private final String[] CITIES = {"DELHI", "MUMBAI", "BANGALORE", "CHENNAI", "KOLKATA"};

    @Scheduled(fixedRate = 5000) // Runs every 5 seconds
    public void simulateTraffic() {
        int roll = random.nextInt(100);
        if (roll < 60) {
            String circle = SCAMMER_CIRCLES[random.nextInt(SCAMMER_CIRCLES.length)];
            int dist = 1900 + random.nextInt(120);
            int dur = 70 + random.nextInt(20);

            saveCall(SCAMMER, circle, dist, dur);
        }

        else if (roll < 85) {
            int dist = 160 + random.nextInt(16);
            int dur = 85 + random.nextInt(10);

            saveCall(DELIVERY_BOY, DELIVERY_CIRCLE, dist, dur);
        }

        else {
            String circle = TELE_CIRCLES[random.nextInt(TELE_CIRCLES.length)];
            int dist = 240 + random.nextInt(20);
            int dur = 170 + random.nextInt(20);

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

