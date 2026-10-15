package com.umasankar.applytrack.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReferralRepository extends JpaRepository<Referral, UUID> {
    List<Referral> findAllByJobApplicationIdAndUserIdOrderByCreatedAtAsc(UUID jobApplicationId, UUID userId);
    Optional<Referral> findByIdAndJobApplicationIdAndUserId(UUID id, UUID jobApplicationId, UUID userId);
    List<Referral> findAllByJobApplicationIdAndUserIdAndStatusOrderByCreatedAtAsc(
        UUID jobApplicationId, UUID userId, ReferralStatus status);
}
