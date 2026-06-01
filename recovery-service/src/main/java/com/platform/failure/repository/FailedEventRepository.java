package com.platform.failure.repository;

import com.platform.failure.model.FailedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FailedEventRepository extends JpaRepository<FailedEventEntity, UUID> {
}
