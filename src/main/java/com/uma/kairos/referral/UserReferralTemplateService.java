package com.umasankar.applytrack.referral;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserReferralTemplateService {
    private final UserReferralTemplateRepository templates;

    public UserReferralTemplateService(UserReferralTemplateRepository templates) {
        this.templates = templates;
    }

    @Transactional
    public UserReferralTemplate findOrCreate(UUID userId) {
        return templates.findById(userId).orElseGet(() ->
            templates.save(new UserReferralTemplate(userId, MessageTemplateRenderer.DEFAULT_TEMPLATE)));
    }

    @Transactional
    public TemplateResponse get(UUID userId) {
        UserReferralTemplate template = findOrCreate(userId);
        return response(template);
    }

    @Transactional
    public TemplateResponse update(UUID userId, String content) {
        UserReferralTemplate template = findOrCreate(userId);
        template.update(content);
        return response(template);
    }

    private TemplateResponse response(UserReferralTemplate template) {
        return new TemplateResponse(template.getTemplate(), template.getUpdatedAt());
    }

    public record TemplateResponse(String template, java.time.Instant updatedAt) {}
}
