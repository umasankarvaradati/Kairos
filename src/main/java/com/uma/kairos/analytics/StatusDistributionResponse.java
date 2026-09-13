package com.umasankar.applytrack.analytics;

import com.umasankar.applytrack.application.ApplicationStatus;

public record StatusDistributionResponse(ApplicationStatus status, long count) {
}
