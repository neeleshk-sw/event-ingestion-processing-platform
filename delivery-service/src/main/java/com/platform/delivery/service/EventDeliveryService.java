package com.platform.delivery.service;

import com.platform.common.model.EventEnvelope;
import com.platform.delivery.client.AuditClient;
import com.platform.delivery.client.FailureRecoveryClient;
import com.platform.delivery.persistence.DeliveryStatePersistenceService;
import com.platform.common.model.FailedEventRecord;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class EventDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(EventDeliveryService.class);

    private final AuditClient auditClient;
    private final DeliveryStatePersistenceService deliveryStatePersistenceService;
    private final FailureRecoveryClient failureClient;
    private final Tracer tracer;

    public EventDeliveryService(AuditClient auditClient,
            DeliveryStatePersistenceService deliveryStatePersistenceService,
            FailureRecoveryClient failureClient,
            OpenTelemetry openTelemetry) {
        this.auditClient = auditClient;
        this.deliveryStatePersistenceService = deliveryStatePersistenceService;
        this.failureClient = failureClient;
        this.tracer = openTelemetry.getTracer(EventDeliveryService.class.getName());
    }

    public void deliver(EventEnvelope eventEnvelope) {

        logger.info("Applying delivery control logic");
        String destination = resolveDestination(eventEnvelope);
        String idempotencyKey = deliveryStatePersistenceService
                .markInProgress(eventEnvelope, destination);
        // Added: audit delivery start
        auditClient.audit(
                eventEnvelope,
                "DELIVERY",
                "STARTED",
                "Delivery processing started");

        Span span = tracer.spanBuilder("external-delivery")
                .setAttribute("destination", destination)
                .setAttribute("eventId", eventEnvelope.getEventId().toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Placeholder for real delivery logic
            simulateDelivery(eventEnvelope);

            logger.info("Delivery successful");
            deliveryStatePersistenceService.markDelivered(idempotencyKey);
            // Added: audit delivery success
            auditClient.audit(
                    eventEnvelope,
                    "DELIVERY",
                    "DELIVERED",
                    "Event delivered successfully");

        } catch (Exception ex) {

            logger.error("Delivery failed", ex);
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);

            deliveryStatePersistenceService.markFailed(
                    idempotencyKey, ex.getMessage());
            // Added: audit delivery failure
            auditClient.audit(
                    eventEnvelope,
                    "DELIVERY",
                    "FAILED",
                    ex.getMessage());

            FailedEventRecord record = new FailedEventRecord();
            record.setEventId(eventEnvelope.getEventId());
            record.setStage("DELIVERY");
            record.setReason(ex.getMessage());
            record.setFailedAt(Instant.now());
            record.setEvent(eventEnvelope);

            failureClient.recordFailure(record);

            throw ex; // propagate failure
        } finally {
            span.end();
        }
    }

    private void simulateDelivery(EventEnvelope eventEnvelope) {
        // TEMP: simulate failure based on attribute
        if ("FAIL".equalsIgnoreCase(
                eventEnvelope.getMetadata()
                        .getAttributes()
                        .get("forceFailure"))) {
            throw new RuntimeException("Simulated delivery failure");
        }
    }

    private String resolveDestination(EventEnvelope eventEnvelope) {
        if (eventEnvelope.getMetadata() == null
                || eventEnvelope.getMetadata().getAttributes() == null) {
            return "DEFAULT_DESTINATION";
        }
        Map<String, String> attributes = eventEnvelope.getMetadata().getAttributes();
        return attributes.getOrDefault("destination", "DEFAULT_DESTINATION");
    }
}
