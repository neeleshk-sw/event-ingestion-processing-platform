package com.platform.audit.model;

import java.time.Instant;
import java.util.UUID;

public class AuditRecord {

    private UUID eventId;
    private String correlationId;
    private String stage;        // INTAKE, VALIDATION, ROUTING, DELIVERY, etc.
    private String action;       // RECEIVED, VALIDATED, ROUTED, FAILED, DELIVERED
    private Instant timestamp;
    private String details;      // free-text, optional

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
