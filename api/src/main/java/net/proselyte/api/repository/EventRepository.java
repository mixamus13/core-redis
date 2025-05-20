package net.proselyte.api.repository;

import net.proselyte.api.entity.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, String> {}
