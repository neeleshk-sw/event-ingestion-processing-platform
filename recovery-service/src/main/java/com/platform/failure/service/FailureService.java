package com.platform.failure.service;

import com.platform.failure.client.AuditClient;
import com.platform.common.model.FailedEventRecord;
import com.platform.failure.persistence.FailedEventPersistenceService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FailureService {

    private static final Logger logger = LoggerFactory.getLogger(FailureService.class);

    private final AuditClient auditClient;
    private final FailedEventPersistenceService failedEventPersistenceService;
    private final Tracer tracer;

    public FailureService(AuditClient auditClient,
            FailedEventPersistenceService failedEventPersistenceService,
            OpenTelemetry openTelemetry) {
        this.auditClient = auditClient;
        this.failedEventPersistenceService = failedEventPersistenceService;
        this.tracer = openTelemetry.getTracer(FailureService.class.getName());
    }

    public void record(FailedEventRecord record) {
        Span span = tracer.spanBuilder("recovery-path")
                .setAttribute("eventId", record.getEventId().toString())
                .setAttribute("stage", record.getStage())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            logger.info(
                    "Failure recorded: eventId={}, stage={}",
                    record.getEventId(),
                    record.getStage());
            failedEventPersistenceService.saveFailure(record);
            // Added: audit failure record persistence path
            if (record.getEvent() != null) {
                auditClient.audit(
                        record.getEvent(),
                        "FAILURE_RECOVERY",
                        "RECORDED",
                        "Failure record accepted for event processing");
            } else {
                auditClient.audit(
                        "FAILURE_RECOVERY",
                        "RECORDED",
                        "Failure record accepted (no event envelope)",
                        record.getEventId().toString());
            }
        } catch (Exception ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            span.end();
        }

        // Placeholder:
        // - persist failure
        // - index for search
        // - allow replay later
    }
}
