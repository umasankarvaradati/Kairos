package com.umasankar.applytrack.referral;

import com.umasankar.applytrack.application.JobApplication;
import com.umasankar.applytrack.application.JobApplicationRepository;
import com.umasankar.applytrack.auth.CurrentUser;
import com.umasankar.applytrack.auth.User;
import com.umasankar.applytrack.auth.UserRepository;
import com.umasankar.applytrack.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReferralService {
    private final ReferralRepository referrals;
    private final JobApplicationRepository applications;
    private final UserRepository users;
    private final MessageTemplateRenderer renderer;
    private final ReferralMailer mailer;
    private final UserReferralTemplateService userTemplates;

    public ReferralService(ReferralRepository referrals, JobApplicationRepository applications,
                           UserRepository users, MessageTemplateRenderer renderer, ReferralMailer mailer,
                           UserReferralTemplateService userTemplates) {
        this.referrals = referrals;
        this.applications = applications;
        this.users = users;
        this.renderer = renderer;
        this.mailer = mailer;
        this.userTemplates = userTemplates;
    }

    public List<ReferralResponse> list(UUID applicationId) {
        UUID userId = CurrentUser.id();
        requireApplication(applicationId, userId);
        return referrals.findAllByJobApplicationIdAndUserIdOrderByCreatedAtAsc(applicationId, userId)
            .stream().map(ReferralResponse::from).toList();
    }

    @Transactional
    public ReferralResponse create(UUID applicationId, ReferralRequest request) {
        UUID userId = CurrentUser.id();
        requireApplication(applicationId, userId);
        String name = clean(request.name());
        String linkedinUrl = clean(request.linkedinUrl());
        String email = clean(request.email());
        boolean duplicate = referrals.findAllByJobApplicationIdAndUserIdOrderByCreatedAtAsc(applicationId, userId)
            .stream().anyMatch(existing -> same(existing.getName(), name)
                && same(existing.getEmail(), email) && same(existing.getLinkedinUrl(), linkedinUrl));
        if (duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This referral contact already exists for this application");
        }
        String messageTemplate = clean(request.messageTemplate());
        if (messageTemplate == null) messageTemplate = userTemplates.findOrCreate(userId).getTemplate();
        Referral referral = new Referral(userId, applicationId, name, linkedinUrl, email, messageTemplate);
        return ReferralResponse.from(referrals.save(referral));
    }

    @Transactional
    public ReferralResponse update(UUID applicationId, UUID referralId, ReferralRequest request) {
        Referral referral = requireReferral(applicationId, referralId, CurrentUser.id());
        referral.update(clean(request.name()), clean(request.linkedinUrl()), clean(request.email()),
            clean(request.messageTemplate()));
        return ReferralResponse.from(referral);
    }

    @Transactional
    public void delete(UUID applicationId, UUID referralId) {
        referrals.delete(requireReferral(applicationId, referralId, CurrentUser.id()));
    }

    public MessagePreview preview(UUID applicationId, UUID referralId) {
        Context context = context(applicationId, referralId, CurrentUser.id());
        return new MessagePreview(renderer.render(context.referral().getMessageTemplate(), context.referral(),
            context.application(), context.user()));
    }

    @Transactional
    public SendResult send(UUID applicationId, UUID referralId, SendRequest request) {
        Context context = context(applicationId, referralId, CurrentUser.id());
        String template = request == null || request.messageTemplate() == null
            ? context.referral().getMessageTemplate() : request.messageTemplate();
        String message = renderer.render(template, context.referral(), context.application(), context.user());
        if (context.referral().getEmail() == null || context.referral().getEmail().isBlank()) {
            return new SendResult(referralId, "GENERATED", message, null, context.referral().getStatus());
        }
        mailer.send(context.referral().getEmail(), "Referral request: " + context.application().getJobTitle(),
            message, context.user());
        context.referral().markSent();
        return new SendResult(referralId, "SENT", message, null, context.referral().getStatus());
    }

    @Transactional
    public List<SendResult> sendAll(UUID applicationId) {
        UUID userId = CurrentUser.id();
        JobApplication application = requireApplication(applicationId, userId);
        User user = requireUser(userId);
        return referrals.findAllByJobApplicationIdAndUserIdAndStatusOrderByCreatedAtAsc(
                applicationId, userId, ReferralStatus.PENDING)
            .stream().map(referral -> sendOne(referral, application, user)).toList();
    }

    @Transactional
    public ReferralResponse markSent(UUID applicationId, UUID referralId) {
        Referral referral = requireReferral(applicationId, referralId, CurrentUser.id());
        referral.markSent();
        return ReferralResponse.from(referral);
    }

    @Transactional
    public ReferralResponse markReferred(UUID applicationId, UUID referralId) {
        Referral referral = requireReferral(applicationId, referralId, CurrentUser.id());
        referral.markReferred();
        return ReferralResponse.from(referral);
    }

    private SendResult sendOne(Referral referral, JobApplication application, User user) {
        String message = renderer.render(referral.getMessageTemplate(), referral, application, user);
        if (referral.getEmail() == null || referral.getEmail().isBlank()) {
            return new SendResult(referral.getId(), "GENERATED", message, null, referral.getStatus());
        }
        try {
            mailer.send(referral.getEmail(), "Referral request: " + application.getJobTitle(), message, user);
            referral.markSent();
            return new SendResult(referral.getId(), "SENT", message, null, referral.getStatus());
        } catch (ResponseStatusException exception) {
            return new SendResult(referral.getId(), "FAILED", null, exception.getReason(), referral.getStatus());
        } catch (RuntimeException exception) {
            return new SendResult(referral.getId(), "FAILED", null, "Email delivery failed", referral.getStatus());
        }
    }

    private Context context(UUID applicationId, UUID referralId, UUID userId) {
        Referral referral = requireReferral(applicationId, referralId, userId);
        JobApplication application = requireApplication(applicationId, userId);
        return new Context(referral, application, requireUser(userId));
    }

    private JobApplication requireApplication(UUID id, UUID userId) {
        return applications.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private Referral requireReferral(UUID applicationId, UUID referralId, UUID userId) {
        requireApplication(applicationId, userId);
        return referrals.findByIdAndJobApplicationIdAndUserId(referralId, applicationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(referralId));
    }

    private User requireUser(UUID userId) {
        return users.findById(userId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private boolean same(String left, String right) {
        if (left == null || right == null) return left == null && right == null;
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private record Context(Referral referral, JobApplication application, User user) {}
    public record MessagePreview(String message) {}
}
