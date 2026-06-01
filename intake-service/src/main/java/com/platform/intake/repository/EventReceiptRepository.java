package com.platform.intake.repository;

import com.platform.intake.model.EventReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventReceiptRepository extends JpaRepository<EventReceipt, UUID> {
}
