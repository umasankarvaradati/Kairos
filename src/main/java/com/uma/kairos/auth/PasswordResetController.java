package com.umasankar.applytrack.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
public class PasswordResetController {
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PasswordResetService.RequestResponse request(@Valid @RequestBody EmailRequest request) {
        return service.requestCode(request.email());
    }

    @PostMapping("/verify")
    public PasswordResetService.VerifyResponse verify(@Valid @RequestBody VerifyRequest request) {
        return service.verifyCode(request.email(), request.code());
    }

    @PostMapping("/complete")
    public AuthController.AuthResponse complete(@Valid @RequestBody CompleteRequest request) {
        return service.completeReset(request.resetToken(), request.newPassword(), request.confirmPassword());
    }

    public record EmailRequest(@NotBlank @Email @Size(max = 320) String email) {}
    public record VerifyRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Pattern(regexp = "\\d{6}") String code
    ) {}
    public record CompleteRequest(
        @NotBlank String resetToken,
        @NotBlank @Size(min = 8, max = 72) String newPassword,
        @NotBlank @Size(min = 8, max = 72) String confirmPassword
    ) {}
}
