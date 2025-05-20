package net.proselyte.api.repository;

import net.proselyte.api.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {}
