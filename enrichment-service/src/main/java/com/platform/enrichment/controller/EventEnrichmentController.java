package com.platform.enrichment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platform.common.model.EventEnvelope;
import com.platform.enrichment.service.EventEnrichmentService;

@RestController
@RequestMapping("/enrich")
public class EventEnrichmentController {

    private final EventEnrichmentService enrichmentService;

    public EventEnrichmentController(EventEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    private static final Logger logger =
            LoggerFactory.getLogger(EventEnrichmentController.class);

    @PostMapping
    public ResponseEntity<EventEnvelope> enrich(
            @RequestBody EventEnvelope eventEnvelope) {

        logger.info("Enriching event");
        return ResponseEntity.ok(
                enrichmentService.enrich(eventEnvelope)
        );
    }
}
