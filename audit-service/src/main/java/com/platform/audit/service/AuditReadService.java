package com.platform.audit.service;

import com.platform.audit.cache.AuditCacheClient;
import com.platform.audit.model.AuditLogEntity;
import com.platform.audit.repository.AuditRepository;
import com.platform.common.model.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuditReadService {

    private static final Logger logger = LoggerFactory.getLogger(AuditReadService.class);

    private final AuditRepository auditRepository;
    private final AuditCacheClient auditCacheClient;

    public AuditReadService(AuditRepository auditRepository, AuditCacheClient auditCacheClient) {
        this.auditRepository = auditRepository;
        this.auditCacheClient = auditCacheClient;
    }

    public Optional<AuditRecord> findByEventId(UUID eventId) {
        logger.info("Reading audit for eventId: {}", eventId);

        // 1. Try Cache
        Optional<AuditRecord> cachedRecord = auditCacheClient.getAudit(eventId);
        if (cachedRecord.isPresent()) {
            logger.info("Audit record found in cache for eventId: {}", eventId);
            return cachedRecord;
        }

        // 2. Try DB
        logger.info("Audit record not in cache, checking DB for eventId: {}", eventId);
        return auditRepository.findAll().stream() // Simplified query for skeleton implementation
                .filter(e -> e.getEventId().equals(eventId))
                .findFirst()
                .map(entity -> {
                    AuditRecord record = mapToRecord(entity);
                    auditCacheClient.cacheAudit(record); // Cache it for next time
                    return record;
                });
    }

    private AuditRecord mapToRecord(AuditLogEntity entity) {
        AuditRecord record = new AuditRecord();
        record.setEventId(entity.getEventId());
        record.setCorrelationId(entity.getCorrelationId());
        record.setStage(entity.getStage());
        record.setAction(entity.getAction());
        record.setDetails(entity.getDetails());
        return record;
    }
}
