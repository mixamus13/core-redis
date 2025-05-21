package net.proselyte.api.repository;

import net.proselyte.api.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<EventJpaEntity, String> {
}
