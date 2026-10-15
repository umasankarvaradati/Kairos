package com.umasankar.applytrack.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReferralTemplateRepository extends JpaRepository<UserReferralTemplate, UUID> {}
