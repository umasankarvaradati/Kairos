package com.umasankar.applytrack.auth;

import com.umasankar.applytrack.application.JobApplicationRepository;
import com.umasankar.applytrack.referral.UserReferralTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserAccountService {
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final UserReferralTemplateService referralTemplates;

    public UserAccountService(UserRepository users, JobApplicationRepository applications,
                              UserReferralTemplateService referralTemplates) {
        this.users = users;
        this.applications = applications;
        this.referralTemplates = referralTemplates;
    }

    @Transactional
    public User findOrCreate(String email, String passwordHash, String name, AuthProvider provider) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User existing = users.findByEmail(normalizedEmail).orElse(null);
        if (existing != null) return existing;

        User created = users.saveAndFlush(new User(normalizedEmail, passwordHash, name, provider));
        applications.claimOwnerlessApplications();
        referralTemplates.findOrCreate(created.getId());
        return created;
    }
}
