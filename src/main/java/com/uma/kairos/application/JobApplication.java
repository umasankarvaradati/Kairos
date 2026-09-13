package com.umasankar.applytrack.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    private UUID id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Column(name = "job_url", length = 2048)
    private String jobUrl;

    @Column(length = 150)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_source", length = 50)
    private ApplicationSource applicationSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JobApplication() {
    }

    public JobApplication(String companyName, String jobTitle, String jobUrl, String location,
                          ApplicationSource applicationSource, ApplicationStatus status,
                          LocalDate applicationDate, String notes) {
        update(companyName, jobTitle, jobUrl, location, applicationSource, status, applicationDate, notes);
    }

    public void update(String companyName, String jobTitle, String jobUrl, String location,
                       ApplicationSource applicationSource, ApplicationStatus status,
                       LocalDate applicationDate, String notes) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobUrl = jobUrl;
        this.location = location;
        this.applicationSource = applicationSource;
        this.status = status;
        this.applicationDate = applicationDate;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getJobTitle() { return jobTitle; }
    public String getJobUrl() { return jobUrl; }
    public String getLocation() { return location; }
    public ApplicationSource getApplicationSource() { return applicationSource; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
