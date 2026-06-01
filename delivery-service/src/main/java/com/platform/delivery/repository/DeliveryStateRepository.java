package com.platform.delivery.repository;

import com.platform.delivery.model.DeliveryState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryStateRepository extends JpaRepository<DeliveryState, String> {
}
