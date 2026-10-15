package com.umasankar.applytrack.referral;

import java.time.Instant;
import java.util.UUID;

public record ReferralResponse(
    UUID id, UUID jobApplicationId, String name, String linkedinUrl, String email,
    String messageTemplate, ReferralStatus status, Instant sentAt, Instant referredAt,
    Instant createdAt, Instant updatedAt
) {
    static ReferralResponse from(Referral referral) {
        return new ReferralResponse(referral.getId(), referral.getJobApplicationId(), referral.getName(),
            referral.getLinkedinUrl(), referral.getEmail(), referral.getMessageTemplate(), referral.getStatus(),
            referral.getSentAt(), referral.getReferredAt(), referral.getCreatedAt(), referral.getUpdatedAt());
    }
}
