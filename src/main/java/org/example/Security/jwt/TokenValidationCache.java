package org.example.Security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenValidationCache {

    private final Cache<String, Boolean> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(10000)
            .build();

    public Boolean get(String token) {
        return tokenCache.getIfPresent(token);
    }

    public void put(String token, Boolean isValid) {
        tokenCache.put(token, isValid);
    }

    public long size() {
        return tokenCache.estimatedSize();
    }

    public void invalidate(String token) {
        tokenCache.invalidate(token);
    }

    public boolean invalidateAndCheck(String token) {
        boolean existed = tokenCache.asMap().containsKey(token);
        tokenCache.invalidate(token);
        return existed;
    }
}
