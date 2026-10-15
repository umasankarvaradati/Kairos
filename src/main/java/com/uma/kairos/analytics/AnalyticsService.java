package com.umasankar.applytrack.analytics;

import com.umasankar.applytrack.auth.CurrentUser;
import com.umasankar.applytrack.application.ApplicationStatus;
import com.umasankar.applytrack.application.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final JobApplicationRepository repository;

    public AnalyticsService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public AnalyticsSummaryResponse summary() {
        Map<ApplicationStatus, Long> counts = statusCounts();
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long accepted = count(counts, ApplicationStatus.ACCEPTED);
        long rejected = count(counts, ApplicationStatus.REJECTED);
        long withdrawn = count(counts, ApplicationStatus.WITHDRAWN);

        return new AnalyticsSummaryResponse(
            total,
            total - accepted - rejected - withdrawn,
            count(counts, ApplicationStatus.SAVED),
            count(counts, ApplicationStatus.APPLIED),
            count(counts, ApplicationStatus.INTERVIEW),
            count(counts, ApplicationStatus.OFFER),
            accepted,
            rejected,
            withdrawn
        );
    }

    public List<StatusDistributionResponse> statusDistribution() {
        Map<ApplicationStatus, Long> counts = statusCounts();
        return Arrays.stream(ApplicationStatus.values())
            .map(status -> new StatusDistributionResponse(status, count(counts, status)))
            .toList();
    }

    private Map<ApplicationStatus, Long> statusCounts() {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        repository.countGroupedByStatus(CurrentUser.id()).forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return counts;
    }

    private long count(Map<ApplicationStatus, Long> counts, ApplicationStatus status) {
        return counts.getOrDefault(status, 0L);
    }
}
