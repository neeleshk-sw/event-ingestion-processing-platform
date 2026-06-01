package com.platform.routing.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryQueueRepository extends JpaRepository<DeliveryQueueEntry, UUID> {
    // Write-only from routing side — no custom queries needed
}
