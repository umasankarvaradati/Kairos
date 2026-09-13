package com.umasankar.applytrack.analytics;

public record AnalyticsSummaryResponse(
    long totalApplications,
    long activeApplications,
    long savedApplications,
    long appliedApplications,
    long interviewApplications,
    long offerApplications,
    long acceptedApplications,
    long rejectedApplications,
    long withdrawnApplications
) {
}
