package com.umasankar.applytrack.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(UUID id) {
        super("Job application with id '%s' was not found".formatted(id));
    }
}
