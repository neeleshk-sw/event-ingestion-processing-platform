package com.platform.intake.service;

import com.platform.common.contract.ValidationResult;
import com.platform.common.exception.InvalidEventException;
import com.platform.common.model.EventEnvelope;
import com.platform.intake.client.AuditClient;
import com.platform.intake.client.ValidationClient;
import com.platform.intake.persistence.EventReceiptPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventIngestionService {
    private static final Logger logger = LoggerFactory.getLogger(EventIngestionService.class);

    private final AuditClient auditClient;
    private final EventReceiptPersistenceService eventReceiptPersistenceService;
    private final ValidationClient validationClient;

    public EventIngestionService(AuditClient auditClient,
            EventReceiptPersistenceService eventReceiptPersistenceService,
            ValidationClient validationClient) {
        this.auditClient = auditClient;
        this.eventReceiptPersistenceService = eventReceiptPersistenceService;
        this.validationClient = validationClient;
    }

    public void accept(EventEnvelope eventEnvelope) {

        ValidationResult result = validationClient.validate(eventEnvelope);

        if (!result.isValid()) {
            throw new InvalidEventException(result.getReason());
        }

        // Next step: forward to Normalization Service
    }

    public void accept(EventEnvelope eventEnvelope, String correlationId) {
        logger.info("Forwarding event to Validation Service.");
        eventReceiptPersistenceService.saveReceipt(eventEnvelope, correlationId);
        // Added: audit for intake receive
        auditClient.audit(
                eventEnvelope,
                "INTAKE",
                "RECEIVED",
                "Event received from producer");

        // Added: audit for downstream handoff
        auditClient.audit(
                eventEnvelope,
                "INTAKE",
                "FORWARDED_TO_VALIDATION",
                "Event forwarded to validation service");
        validationClient.validate(eventEnvelope, correlationId);
    }

}
