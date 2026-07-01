package com.offerflow.copilot.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "project", "offerflow-copilot",
                "phase", "P1A+P1B",
                "dataMode", "mock/local-rule",
                "timestamp", Instant.now().toString());
    }
}
