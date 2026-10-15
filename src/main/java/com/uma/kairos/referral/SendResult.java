package com.umasankar.applytrack.referral;

import java.util.UUID;

public record SendResult(UUID referralId, String outcome, String message, String error, ReferralStatus status) {}
