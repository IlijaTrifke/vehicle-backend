package com.meuhlbauer.vehicle_backend.exception;

public class NotFoundException extends RuntimeException {
    private final String resource;
    private final String resourceId;

    public NotFoundException(String resource, String resourceId, String message) {
        super(message);
        this.resource = resource;
        this.resourceId = resourceId;
    }

    public String getResource() {
        return resource;
    }

    public String getResourceId() {
        return resourceId;
    }
}