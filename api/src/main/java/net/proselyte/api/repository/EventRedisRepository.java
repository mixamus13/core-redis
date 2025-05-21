package net.proselyte.api.repository;

import net.proselyte.api.entity.EventRedisEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventRedisRepository extends CrudRepository<EventRedisEntity, String> {}
