package com.umasankar.applytrack.referral;

import jakarta.validation.constraints.Size;

public record SendRequest(@Size(max = 10000) String messageTemplate) {}
