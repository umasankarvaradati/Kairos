package com.umasankar.applytrack.referral;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ReferralRequest(
    @NotBlank @Size(max = 150) String name,
    @URL @Size(max = 2048) String linkedinUrl,
    @Email @Size(max = 320) String email,
    @Size(max = 10000) String messageTemplate
) {}
