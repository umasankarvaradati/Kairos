package com.umasankar.applytrack.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.Base64;

@Service
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String GENERIC_REQUEST_MESSAGE = "If an account exists for that email, a reset code has been sent.";
    private static final int MAX_OTP_ATTEMPTS = 5;
    private final UserRepository users;
    private final PasswordResetChallengeRepository challenges;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailer mailer;
    private final JwtService jwtService;

    public PasswordResetService(UserRepository users, PasswordResetChallengeRepository challenges,
                                PasswordEncoder passwordEncoder, PasswordResetMailer mailer,
                                JwtService jwtService) {
        this.users = users;
        this.challenges = challenges;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.jwtService = jwtService;
    }

    @Transactional
    public RequestResponse requestCode(String email) {
        String normalizedEmail = normalize(email);
        User user = users.findByEmail(normalizedEmail).orElse(null);
        if (user == null) return new RequestResponse(GENERIC_REQUEST_MESSAGE);

        Instant now = Instant.now();
        challenges.deleteByCreatedAtBefore(now.minus(1, ChronoUnit.DAYS));
        var previous = challenges.findTopByUserIdOrderByCreatedAtDesc(user.getId());
        if (previous.isPresent() && previous.get().getCreatedAt().isAfter(now.minus(60, ChronoUnit.SECONDS))) {
            return new RequestResponse(GENERIC_REQUEST_MESSAGE);
        }
        if (challenges.countByUserIdAndCreatedAtAfter(user.getId(), now.minus(1, ChronoUnit.HOURS)) >= 5) {
            return new RequestResponse(GENERIC_REQUEST_MESSAGE);
        }

        for (PasswordResetChallenge active : challenges.findAllByUserIdAndConsumedAtIsNull(user.getId())) {
            active.consume(now);
        }
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        PasswordResetChallenge challenge = challenges.save(new PasswordResetChallenge(user.getId(),
            passwordEncoder.encode(code), now.plus(10, ChronoUnit.MINUTES)));
        if (!mailer.sendCode(normalizedEmail, code)) challenge.consume(Instant.now());
        return new RequestResponse(GENERIC_REQUEST_MESSAGE);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public VerifyResponse verifyCode(String email, String code) {
        User user = users.findByEmail(normalize(email)).orElse(null);
        if (user == null) throw invalidCode();

        PasswordResetChallenge challenge = challenges.lockActiveForUser(user.getId()).stream()
            .findFirst().orElseThrow(this::invalidCode);
        Instant now = Instant.now();
        if (challenge.getExpiresAt().isBefore(now) || challenge.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            challenge.consume(now);
            throw invalidCode();
        }
        if (!passwordEncoder.matches(code, challenge.getOtpHash())) {
            challenge.countFailedAttempt();
            if (challenge.getAttemptCount() >= MAX_OTP_ATTEMPTS) challenge.consume(now);
            throw invalidCode();
        }

        byte[] resetTokenBytes = new byte[32];
        RANDOM.nextBytes(resetTokenBytes);
        String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(resetTokenBytes);
        challenge.verify(sha256(resetToken), now);
        return new VerifyResponse(resetToken);
    }

    @Transactional
    public AuthController.AuthResponse completeReset(String resetToken, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        PasswordResetChallenge challenge = challenges.lockByResetTokenHash(sha256(resetToken))
            .orElseThrow(this::invalidToken);
        Instant now = Instant.now();
        if (challenge.getVerifiedAt() == null || challenge.getExpiresAt().isBefore(now)) {
            challenge.consume(now);
            throw invalidToken();
        }
        User user = users.findById(challenge.getUserId()).orElseThrow(this::invalidToken);
        user.changePasswordHash(passwordEncoder.encode(newPassword));
        challenge.consume(now);
        return new AuthController.AuthResponse(jwtService.issue(user), AuthController.UserView.from(user));
    }

    private ResponseStatusException invalidCode() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "The code is invalid or expired");
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password reset session is invalid or expired");
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record RequestResponse(String message) {}
    public record VerifyResponse(String resetToken) {}
}
