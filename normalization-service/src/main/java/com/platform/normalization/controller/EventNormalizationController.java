package com.platform.normalization.controller;

import com.platform.common.contract.NormalizationResult;
import com.platform.common.model.EventEnvelope;
import com.platform.normalization.service.EventNormalizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/normalize")
public class EventNormalizationController {
    private static final Logger logger = LoggerFactory.getLogger(EventNormalizationController.class);

    private final EventNormalizationService normalizationService;

    public EventNormalizationController(EventNormalizationService normalizationService) {
        this.normalizationService = normalizationService;
    }
    
    @PostMapping
    public ResponseEntity<NormalizationResult> normalize(
            @RequestBody EventEnvelope eventEnvelope) {

        logger.info("Received normalization request");

        NormalizationResult result =
                normalizationService.normalize(eventEnvelope);

        return ResponseEntity.ok(result);
    }

}
