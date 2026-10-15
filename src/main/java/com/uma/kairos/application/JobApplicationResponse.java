package com.umasankar.applytrack.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobApplicationResponse(
    UUID id,
    String jobId,
    String companyName,
    String jobTitle,
    String jobUrl,
    String location,
    ApplicationSource applicationSource,
    ApplicationStatus status,
    LocalDate applicationDate,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {
    static JobApplicationResponse from(JobApplication application) {
        return new JobApplicationResponse(
            application.getId(), application.getJobId(), application.getCompanyName(), application.getJobTitle(), application.getJobUrl(),
            application.getLocation(), application.getApplicationSource(), application.getStatus(),
            application.getApplicationDate(), application.getNotes(), application.getCreatedAt(), application.getUpdatedAt()
        );
    }
}
