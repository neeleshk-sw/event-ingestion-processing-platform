package com.platform.audit.repository;

import com.platform.audit.model.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditLogEntity, UUID> {
    // Additional query methods can be added here
}
