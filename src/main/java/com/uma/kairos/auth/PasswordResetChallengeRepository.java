package com.umasankar.applytrack.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetChallengeRepository extends JpaRepository<PasswordResetChallenge, UUID> {
    Optional<PasswordResetChallenge> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant since);
    List<PasswordResetChallenge> findAllByUserIdAndConsumedAtIsNull(UUID userId);
    int deleteByCreatedAtBefore(Instant before);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from PasswordResetChallenge challenge where challenge.userId = :userId and challenge.consumedAt is null order by challenge.createdAt desc")
    List<PasswordResetChallenge> lockActiveForUser(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from PasswordResetChallenge challenge where challenge.resetTokenHash = :hash and challenge.consumedAt is null")
    Optional<PasswordResetChallenge> lockByResetTokenHash(@Param("hash") String hash);
}
