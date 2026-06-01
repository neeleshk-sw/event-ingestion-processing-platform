package com.platform.common.model;

import java.time.Instant;
import java.util.UUID;

public class FailedEventRecord {

    private UUID eventId;
    private String stage; // VALIDATION, ROUTING, DELIVERY, etc.
    private String reason;
    private Instant failedAt;
    private EventEnvelope event;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public EventEnvelope getEvent() {
        return event;
    }

    public void setEvent(EventEnvelope event) {
        this.event = event;
    }
}
