package com.platform.audit.controller;

import com.platform.common.model.AuditRecord;
import com.platform.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<Void> record(
            @RequestBody AuditRecord record) {

        logger.info(
                "Audit record received: eventId={}, stage={}, action={}",
                record.getEventId(),
                record.getStage(),
                record.getAction());

        auditService.record(record);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<Page<AuditRecord>> getAudits(Pageable pageable) {
        logger.info("Fetching paginated audits: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(auditService.findAll(pageable));
    }
}
