package com.umasankar.applytrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SpringBootApplication
public class ApplyTrackApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplyTrackApplication.class);
        application.addInitializers(context -> {
            validateDatabaseTarget(context.getEnvironment());
            validateJwtSecret(context.getEnvironment());
        });
        application.run(args);
    }

    private static void validateJwtSecret(ConfigurableEnvironment environment) {
        String secret = environment.getProperty("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not set. Add a persistent random secret to the application environment.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes.");
        }
    }

    private static void validateDatabaseTarget(ConfigurableEnvironment environment) {
        String jdbcUrl = environment.getProperty("DATABASE_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL is not set. Set it to the Neon connection URL in the application environment.");
        }
        String uriValue;
        boolean standardPostgresUrl = jdbcUrl.startsWith("postgresql://");
        if (jdbcUrl.startsWith("jdbc:postgresql://")) {
            uriValue = jdbcUrl.substring("jdbc:".length());
        } else if (standardPostgresUrl) {
            uriValue = jdbcUrl;
        } else {
            throw new IllegalStateException("DATABASE_URL must be a PostgreSQL JDBC URL or Neon postgresql:// connection URL");
        }
        try {
            URI target = new URI(uriValue);
            String database = target.getPath() == null ? "" : target.getPath().replaceFirst("^/", "");
            if (!"ep-broad-heart-ay53aflf-pooler.c-5.us-east-2.aws.neon.tech".equalsIgnoreCase(target.getHost())
                    || !"neondb".equals(database) || target.getFragment() != null) {
                throw new IllegalStateException("DATABASE_URL must target the configured Neon host and neondb database");
            }
            if (standardPostgresUrl) {
                // Neon supplies credentials in its URI; JDBC receives credentials separately from env vars.
                URI jdbcTarget = new URI("postgresql", null, target.getHost(), target.getPort(),
                        target.getPath(), target.getQuery(), null);
                environment.getPropertySources().addFirst(new MapPropertySource("normalizedDatabaseUrl",
                        Map.of("spring.datasource.url", "jdbc:" + jdbcTarget.toASCIIString())));
            } else if (target.getUserInfo() != null) {
                throw new IllegalStateException("Put database credentials in DATABASE_USERNAME and DATABASE_PASSWORD, not DATABASE_URL");
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("DATABASE_URL is malformed", exception);
        }
    }
}
