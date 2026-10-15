package com.umasankar.applytrack.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final long ttlHours;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.ttl-hours:24}") long ttlHours) {
        this.encoder = encoder;
        this.ttlHours = ttlHours;
    }

    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("kairos").issuedAt(now).expiresAt(now.plus(ttlHours, ChronoUnit.HOURS))
            .subject(user.getId().toString()).claim("email", user.getEmail()).claim("name", user.getName()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
