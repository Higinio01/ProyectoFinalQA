package org.example.Security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class TokenValidationCache {

    private final Cache<String, Boolean> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(10000)
            .build();

    public Boolean get(String token) {
        return tokenCache.getIfPresent(token);
    }

    public void put(String token, Boolean isValid) {
        tokenCache.put(token, isValid);
    }

    public void invalidate(String token) {
        tokenCache.invalidate(token);
    }

    public long size() {
        return tokenCache.estimatedSize();
    }

    // 🧪 MÉTODO DE PRUEBA TEMPORAL
    public void testCache() {
        System.out.println("🧪 PROBANDO CACHE...");
        tokenCache.put("test-token", true);
        System.out.println("🧪 Guardado test-token = true");
        System.out.println("🧪 Tamaño después de guardar: " + tokenCache.estimatedSize());
        Boolean result = tokenCache.getIfPresent("test-token");
        System.out.println("🧪 Recuperado test-token = " + result);
    }
}
