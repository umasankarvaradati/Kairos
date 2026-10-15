package com.umasankar.applytrack.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 320) private String email;
    @Column(name = "password_hash", length = 100) private String passwordHash;
    @Column(nullable = false, length = 150) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AuthProvider provider;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected User() {}

    public User(String email, String passwordHash, String name, AuthProvider provider) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.provider = provider;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public AuthProvider getProvider() { return provider; }
    public Instant getCreatedAt() { return createdAt; }
}
