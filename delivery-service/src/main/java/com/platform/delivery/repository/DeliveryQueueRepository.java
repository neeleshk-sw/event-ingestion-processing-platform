package com.platform.delivery.repository;

import com.platform.delivery.model.DeliveryQueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryQueueRepository extends JpaRepository<DeliveryQueueEntry, UUID> {

    List<DeliveryQueueEntry> findTop10ByStatusOrderByQueuedAtAsc(String status);

    @Modifying
    @Query("UPDATE DeliveryQueueEntry e SET e.status = :status WHERE e.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status);
}
