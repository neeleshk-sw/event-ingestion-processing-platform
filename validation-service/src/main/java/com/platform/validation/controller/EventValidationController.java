package com.platform.validation.controller;

import com.platform.common.contract.ValidationResult;
import com.platform.common.model.EventEnvelope;
import com.platform.validation.service.EventValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/validate")
public class EventValidationController {
    private static final Logger logger = LoggerFactory.getLogger(EventValidationController.class);

    private final EventValidationService validationService;

    public EventValidationController(EventValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping
    public ResponseEntity<ValidationResult> validate(
            @RequestHeader("X-Correlation-Id") String correlationId,
            @RequestBody EventEnvelope eventEnvelope) {

    	logger.info("Validating event.");

        ValidationResult result =
                validationService.validate(eventEnvelope, correlationId);

        return ResponseEntity.ok(result);
    }

}
