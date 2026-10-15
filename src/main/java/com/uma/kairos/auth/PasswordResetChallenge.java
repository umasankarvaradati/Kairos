package com.umasankar.applytrack.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_challenges")
public class PasswordResetChallenge {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "otp_hash", nullable = false, length = 100) private String otpHash;
    @Column(name = "reset_token_hash", length = 64) private String resetTokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "verified_at") private Instant verifiedAt;
    @Column(name = "consumed_at") private Instant consumedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected PasswordResetChallenge() {}

    public PasswordResetChallenge(UUID userId, String otpHash, Instant expiresAt) {
        this.userId = userId;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public void countFailedAttempt() { attemptCount++; }
    public void verify(String tokenHash, Instant time) { resetTokenHash = tokenHash; verifiedAt = time; }
    public void consume(Instant time) { consumedAt = time; }
    public UUID getUserId() { return userId; }
    public String getOtpHash() { return otpHash; }
    public String getResetTokenHash() { return resetTokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
