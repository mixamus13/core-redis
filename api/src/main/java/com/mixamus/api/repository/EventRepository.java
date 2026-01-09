package com.mixamus.api.repository;

import com.mixamus.api.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, String> {
}
