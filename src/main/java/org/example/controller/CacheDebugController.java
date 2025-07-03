package org.example.controller;

import org.example.Security.jwt.TokenValidationCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/cache")
public class CacheDebugController {

    private final TokenValidationCache tokenValidationCache;

    public CacheDebugController(TokenValidationCache tokenValidationCache) {
        this.tokenValidationCache = tokenValidationCache;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        return ResponseEntity.ok(Map.of(
                "size", tokenValidationCache.size()
        ));
    }
}
