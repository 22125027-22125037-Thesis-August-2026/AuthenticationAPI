package com.mhsa.backend.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    // Lưu token vào Blacklist
    public void blacklistToken(String token, long expirationTimeInMillis) {
        long timeToLive = expirationTimeInMillis - System.currentTimeMillis();
        
        if (timeToLive > 0) {
            // Key: "blacklist:<token>", Value: "logout", TTL: timeToLive
            redisTemplate.opsForValue().set(
                "blacklist:" + token, 
                "logout", 
                Duration.ofMillis(timeToLive)
            );
        }
    }

    // Kiểm tra token có nằm trong Blacklist không
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }
}
