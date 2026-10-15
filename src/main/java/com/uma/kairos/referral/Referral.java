package com.umasankar.applytrack.referral;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "referrals")
public class Referral {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "job_application_id", nullable = false) private UUID jobApplicationId;
    @Column(nullable = false, length = 150) private String name;
    @Column(name = "linkedin_url", length = 2048) private String linkedinUrl;
    @Column(length = 320) private String email;
    @Column(name = "message_template", columnDefinition = "TEXT") private String messageTemplate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ReferralStatus status;
    @Column(name = "sent_at") private Instant sentAt;
    @Column(name = "referred_at") private Instant referredAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Referral() {}

    public Referral(UUID userId, UUID jobApplicationId, String name, String linkedinUrl, String email, String messageTemplate) {
        this.userId = userId;
        this.jobApplicationId = jobApplicationId;
        update(name, linkedinUrl, email, messageTemplate);
        this.status = ReferralStatus.PENDING;
    }

    public void update(String name, String linkedinUrl, String email, String messageTemplate) {
        this.name = name;
        this.linkedinUrl = linkedinUrl;
        this.email = email;
        this.messageTemplate = messageTemplate;
    }

    public void markSent() { status = ReferralStatus.MESSAGE_SENT; sentAt = Instant.now(); }
    public void markReferred() { status = ReferralStatus.REFERRED; referredAt = Instant.now(); }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getJobApplicationId() { return jobApplicationId; }
    public String getName() { return name; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getEmail() { return email; }
    public String getMessageTemplate() { return messageTemplate; }
    public ReferralStatus getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
    public Instant getReferredAt() { return referredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
