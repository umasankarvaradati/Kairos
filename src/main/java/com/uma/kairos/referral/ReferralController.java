package com.umasankar.applytrack.referral;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/referrals")
public class ReferralController {
    private final ReferralService service;

    public ReferralController(ReferralService service) { this.service = service; }

    @GetMapping
    public List<ReferralResponse> list(@PathVariable UUID applicationId) { return service.list(applicationId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReferralResponse create(@PathVariable UUID applicationId, @Valid @RequestBody ReferralRequest request) {
        return service.create(applicationId, request);
    }

    @PutMapping("/{id}")
    public ReferralResponse update(@PathVariable UUID applicationId, @PathVariable UUID id,
                                   @Valid @RequestBody ReferralRequest request) {
        return service.update(applicationId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID applicationId, @PathVariable UUID id) {
        service.delete(applicationId, id);
    }

    @GetMapping("/{id}/message")
    public ReferralService.MessagePreview preview(@PathVariable UUID applicationId, @PathVariable UUID id) {
        return service.preview(applicationId, id);
    }

    @PostMapping("/{id}/send")
    public SendResult send(@PathVariable UUID applicationId, @PathVariable UUID id,
                           @Valid @RequestBody(required = false) SendRequest request) {
        return service.send(applicationId, id, request);
    }

    @PostMapping("/send-all")
    public List<SendResult> sendAll(@PathVariable UUID applicationId) {
        return service.sendAll(applicationId);
    }

    @PostMapping("/{id}/mark-sent")
    public ReferralResponse markSent(@PathVariable UUID applicationId, @PathVariable UUID id) {
        return service.markSent(applicationId, id);
    }

    @PostMapping("/{id}/mark-referred")
    public ReferralResponse markReferred(@PathVariable UUID applicationId, @PathVariable UUID id) {
        return service.markReferred(applicationId, id);
    }
}
