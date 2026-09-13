package com.umasankar.applytrack.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse summary() {
        return service.summary();
    }

    @GetMapping("/status-distribution")
    public List<StatusDistributionResponse> statusDistribution() {
        return service.statusDistribution();
    }
}
