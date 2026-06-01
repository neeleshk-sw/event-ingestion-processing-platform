package com.platform.audit.persistence;

import com.platform.audit.model.AuditLogEntity;
import com.platform.audit.repository.AuditLogRepository;
import com.platform.common.model.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditLogPersistenceService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogPersistenceService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void saveAudit(AuditRecord record) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAuditId(UUID.randomUUID());
        entity.setEventId(record.getEventId());
        entity.setCorrelationId(record.getCorrelationId());
        entity.setStage(record.getStage());
        entity.setAction(record.getAction());
        entity.setDetails(record.getDetails());
        entity.setRecordedAt(resolveRecordedAt(record));
        entity.setBatchId(record.getBatchId());
        entity.setTraceId(record.getTraceId());
        entity.setRetryCount(record.getRetryCount());
        entity.setSource(record.getSource());

        auditLogRepository.save(entity);
    }

    public Page<AuditRecord> getAllAudits(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::mapToRecord);
    }

    private AuditRecord mapToRecord(AuditLogEntity entity) {
        AuditRecord record = new AuditRecord();
        record.setEventId(entity.getEventId());
        record.setCorrelationId(entity.getCorrelationId());
        record.setStage(entity.getStage());
        record.setAction(entity.getAction());
        record.setDetails(entity.getDetails());
        record.setTimestamp(entity.getRecordedAt());
        return record;
    }

    private Instant resolveRecordedAt(AuditRecord record) {
        return record.getTimestamp() != null ? record.getTimestamp() : Instant.now();
    }
}
