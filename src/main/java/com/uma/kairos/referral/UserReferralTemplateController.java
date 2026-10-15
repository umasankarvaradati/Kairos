package com.umasankar.applytrack.referral;

import com.umasankar.applytrack.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/referral-template")
public class UserReferralTemplateController {
    private final UserReferralTemplateService service;

    public UserReferralTemplateController(UserReferralTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public UserReferralTemplateService.TemplateResponse get() {
        return service.get(CurrentUser.id());
    }

    @PutMapping
    public UserReferralTemplateService.TemplateResponse update(@Valid @RequestBody UpdateRequest request) {
        if (request.template().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template cannot be blank");
        }
        return service.update(CurrentUser.id(), request.template().trim());
    }

    public record UpdateRequest(@NotBlank @Size(max = 10000) String template) {}
}
