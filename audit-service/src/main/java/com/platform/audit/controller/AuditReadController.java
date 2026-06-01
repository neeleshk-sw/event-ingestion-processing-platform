package com.platform.audit.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.audit.service.AuditReadService;
import com.platform.common.model.AuditRecord;

@RestController
@RequestMapping("/audit")
public class AuditReadController {

    private static final Logger logger = LoggerFactory.getLogger(AuditReadController.class);

    private final AuditReadService auditReadService;

    public AuditReadController(AuditReadService auditReadService) {
        this.auditReadService = auditReadService;
    }

    @GetMapping("/event")
    public ResponseEntity<AuditRecord> getAudit(@RequestParam ("eventId") UUID eventId) {
        logger.info("Received request to fetch audit for eventId: {}", eventId);
        return auditReadService.findByEventId(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
