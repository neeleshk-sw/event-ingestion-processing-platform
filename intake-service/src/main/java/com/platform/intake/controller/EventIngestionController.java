package com.platform.intake.controller;

import com.platform.common.model.EventEnvelope;
import com.platform.common.model.EventMetadata;
import com.platform.common.model.EventStatus;
import com.platform.common.model.ProcessingResult;
import com.platform.intake.service.EventIngestionService;
import com.platform.common.util.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventIngestionController {
    private static final Logger logger = LoggerFactory.getLogger(EventIngestionController.class);

    private final EventIngestionService ingestionService;

    public EventIngestionController(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<ProcessingResult> ingestEvent(
            @RequestBody EventEnvelope eventEnvelope) {

        String correlationId = eventEnvelope.getMetadata().getCorrelationId();
        logger.info("Received event via REST API.");

        EventMetadata metadata = eventEnvelope.getMetadata();
        if (metadata == null) {
            metadata = new EventMetadata();
            eventEnvelope.setMetadata(metadata);
        }
        if (metadata.getTraceId() == null) {
            metadata.setTraceId(java.util.UUID.randomUUID().toString());
        }
        metadata.setRetryCount(0);
        metadata.setSource("REST_API");

        MdcUtil.syncMdc(eventEnvelope);

        ingestionService.accept(eventEnvelope, correlationId);

        return ResponseEntity.accepted()
                .body(new ProcessingResult(
                        EventStatus.RECEIVED,
                        "Event received successfully"));
    }

}
