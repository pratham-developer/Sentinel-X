package com.pratham.sentinelx.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class PhoneCheckUtil {

    private final String keyPrefix = "blacklist:";
    private final RedisTemplate<String, Object> redisTemplate;

    private String getKey(String phone){
        return keyPrefix+phone;
    }

    public void blacklist(String phone) {
        try {
            String key = getKey(phone);
            redisTemplate.opsForValue()
                    .set(key, "1", 1, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new RuntimeException("Blacklist operation failed", e);
        }

    }

    public boolean isBlacklisted(String phone) {
        try {
            Boolean hasKey = redisTemplate.hasKey(getKey(phone));
            return Boolean.TRUE.equals(hasKey); // Avoid NPE
        } catch (Exception e) {
            // Log error but return false to fail-open (allow call) if Redis dies
            System.err.println("Redis Down: " + e.getMessage());
            return false;
        }
    }


}