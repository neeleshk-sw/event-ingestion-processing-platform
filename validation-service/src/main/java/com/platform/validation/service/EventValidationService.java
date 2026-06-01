package com.platform.validation.service;

import com.platform.common.contract.NormalizationResult;
import com.platform.common.contract.ValidationResult;
import com.platform.common.model.EventEnvelope;
import com.platform.validation.client.AuditClient;
import com.platform.validation.client.NormalizationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventValidationService {
    private static final Logger logger = LoggerFactory.getLogger(EventValidationService.class);

    private final AuditClient auditClient;
    private final NormalizationClient normalizationClient;

    public EventValidationService(AuditClient auditClient,
                                  NormalizationClient normalizationClient) {
        this.auditClient = auditClient;
        this.normalizationClient = normalizationClient;
    }

    public ValidationResult validate(EventEnvelope eventEnvelope) {

        // Placeholder validation logic
        ValidationResult validationResult =
                new ValidationResult(true, "Event is valid");

        if (!validationResult.isValid()) {
            return validationResult;
        }

        // Forward to Normalization Service
        NormalizationResult normalizationResult =
                normalizationClient.normalize(eventEnvelope);

        // Normalization result is not exposed upward yet
        return validationResult;
    }
    
    public ValidationResult validate(EventEnvelope eventEnvelope, String correlationId) {

        logger.info("Event validation started.");
        // Added: audit validation start
        auditClient.audit(
                "VALIDATION",
                "STARTED",
                "Validation started",
                eventEnvelope.getEventId().toString()
        );

        ValidationResult validationResult =
                new ValidationResult(true, "Event is valid");

        if (!validationResult.isValid()) {
            // Added: audit validation failure
            auditClient.audit(
                    "VALIDATION",
                    "FAILED",
                    validationResult.getReason(),
                    eventEnvelope.getEventId().toString()
            );
            return validationResult;
        }

        // Added: audit validation success
        auditClient.audit(
                "VALIDATION",
                "PASSED",
                "Event is valid",
                eventEnvelope.getEventId().toString()
        );

        logger.info("Forwarding event to Normalization Service.");
        // Added: audit downstream handoff
        auditClient.audit(
                "VALIDATION",
                "FORWARDED_TO_NORMALIZATION",
                "Event forwarded to normalization service",
                eventEnvelope.getEventId().toString()
        );
        normalizationClient.normalize(eventEnvelope, correlationId);

        return validationResult;
    }

}
