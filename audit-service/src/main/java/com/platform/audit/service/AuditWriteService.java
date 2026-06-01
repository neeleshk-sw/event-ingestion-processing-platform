package com.platform.audit.service;

import com.platform.audit.cache.AuditCacheClient;
import com.platform.common.model.AuditRecord;
import com.platform.audit.model.AuditLogEntity;
import com.platform.audit.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditWriteService {

    private static final Logger logger = LoggerFactory.getLogger(AuditWriteService.class);

    private final AuditRepository auditRepository;
    private final AuditCacheClient auditCacheClient;

    public AuditWriteService(AuditRepository auditRepository, AuditCacheClient auditCacheClient) {
        this.auditRepository = auditRepository;
        this.auditCacheClient = auditCacheClient;
    }

    @Transactional
    public void record(AuditRecord record) {
        logger.info("Recording audit to DB for eventId: {}", record.getEventId());

        AuditLogEntity entity = new AuditLogEntity();
        entity.setAuditId(UUID.randomUUID());
        entity.setEventId(record.getEventId());
        entity.setCorrelationId(record.getCorrelationId());
        entity.setStage(record.getStage());
        entity.setAction(record.getAction());
        entity.setDetails(record.getDetails());
        entity.setRecordedAt(Instant.now());

        auditRepository.save(entity);

        // Update cache with the written record
        try {
            auditCacheClient.cacheAudit(record);
        } catch (Exception e) {
            logger.error("Failed to update cache after DB write for eventId: {}", record.getEventId(), e);
        }
    }
}
