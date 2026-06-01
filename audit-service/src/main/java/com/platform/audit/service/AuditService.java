package com.platform.audit.service;

import com.platform.common.model.AuditRecord;
import com.platform.audit.persistence.AuditLogPersistenceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogPersistenceService auditLogPersistenceService;

    public AuditService(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    public void record(AuditRecord record) {
        auditLogPersistenceService.saveAudit(record);
    }

    public Page<AuditRecord> findAll(Pageable pageable) {
        return auditLogPersistenceService.getAllAudits(pageable);
    }
}
