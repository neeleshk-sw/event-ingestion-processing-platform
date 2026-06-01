package com.platform.failure.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "failed_events")
public class FailedEventEntity {

    @Id
    @Column(name = "failure_id", nullable = false)
    private UUID failureId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "stage", nullable = false)
    private String stage;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "source")
    private String source;

    public UUID getFailureId() {
        return failureId;
    }

    public void setFailureId(UUID failureId) {
        this.failureId = failureId;
    }

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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = (retryCount != null) ? retryCount : 0;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
