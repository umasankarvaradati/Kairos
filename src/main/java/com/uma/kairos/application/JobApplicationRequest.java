package com.umasankar.applytrack.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record JobApplicationRequest(
    @Size(max = 100) String jobId,
    @NotBlank @Size(max = 150) String companyName,
    @NotBlank @Size(max = 150) String jobTitle,
    @URL @Size(max = 2048) String jobUrl,
    @Size(max = 150) String location,
    ApplicationSource applicationSource,
    @NotNull ApplicationStatus status,
    @NotNull @PastOrPresent LocalDate applicationDate,
    @Size(max = 10_000) String notes
) {
}
