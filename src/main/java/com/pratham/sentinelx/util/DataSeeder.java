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

    // Actor Phone Numbers
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

        String[] scammerCircles = {"DELHI", "MUMBAI", "CHENNAI", "KOLKATA"};
        for (int i = 0; i < 60; i++) { // Frequency 60
            logs.add(createLog(
                    SCAMMER_PHONE,
                    scammerCircles[random.nextInt(scammerCircles.length)],
                    1900 + random.nextInt(120),
                    60 + random.nextInt(40)
            ));
        }


        String deliveryCircle = "BANGALORE";
        for (int i = 0; i < 24; i++) {
            logs.add(createLog(
                    DELIVERY_PHONE,
                    deliveryCircle,
                    150 + random.nextInt(36),
                    80 + random.nextInt(20)
            ));
        }

        String[] teleCircles = {"PUNE", "MUMBAI"};
        for (int i = 0; i < 8; i++) {
            logs.add(createLog(
                    TELEMARKETER_PHONE,
                    teleCircles[random.nextInt(teleCircles.length)],
                    230 + random.nextInt(40),
                    160 + random.nextInt(40)
            ));
        }

        repository.saveAll(logs);
        System.out.println("DATABASE SEEDED: Scammer, Delivery, and Telemarketer profiles loaded.");
    }

    private CallLog createLog(String caller, String circle, int dist, int dur) {
        CallLog log = new CallLog();
        log.setCallerNumber(caller);
        log.setReceiverCircle(circle);
        log.setReceiverNumber("+91" + (6000000000L + random.nextInt(900000000))); // Random receiver
        log.setDistanceKm(dist);
        log.setDuration(dur);
        log.setTimestamp(LocalDateTime.now().minusHours(random.nextInt(24)));
        return log;
    }
}