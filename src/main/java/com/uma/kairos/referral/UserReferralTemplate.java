package com.umasankar.applytrack.referral;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_referral_templates")
public class UserReferralTemplate {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String template;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserReferralTemplate() {}

    public UserReferralTemplate(UUID userId, String template) {
        this.userId = userId;
        this.template = template;
    }

    public void update(String template) {
        this.template = template;
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getUserId() { return userId; }
    public String getTemplate() { return template; }
    public Instant getUpdatedAt() { return updatedAt; }
}
