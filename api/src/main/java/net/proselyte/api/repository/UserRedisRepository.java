package net.proselyte.api.repository;

import net.proselyte.api.entity.UserRedisEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRedisRepository extends CrudRepository<UserRedisEntity, String> {}
