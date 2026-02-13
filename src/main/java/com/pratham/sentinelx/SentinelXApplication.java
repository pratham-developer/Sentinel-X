package com.pratham.sentinelx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SentinelXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelXApplication.class, args);
    }

}
