package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.EventDto;
import net.proselyte.api.entity.EventJpaEntity;
import net.proselyte.api.mapper.EventMapper;
import net.proselyte.api.repository.EventJpaRepository;
import net.proselyte.api.repository.EventRedisRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventJpaRepository eventJpaRepository;
    private final EventRedisRepository eventRedisRepository;
    private final EventMapper eventMapper;

    public EventDto create(EventDto dto) {
        log.info("Saving Event to Postgres and Redis");
        EventDto withIdDto = new EventDto(UUID.randomUUID().toString(), dto.title(), dto.description());
        EventJpaEntity saved = eventJpaRepository.save(eventMapper.toJpaEntity(withIdDto));
        eventRedisRepository.save(eventMapper.toRedisEntity(withIdDto));
        return eventMapper.toDto(saved);
    }

    public EventDto get(String id) {
        log.info("Trying to get Event from Redis with id={}", id);

        return eventRedisRepository.findById(id)
                .map(cached -> {
                    log.info("Cache hit for Event id={}", id);
                    return eventMapper.toDto(cached);
                })
                .orElseGet(() -> {
                    log.info("Cache miss for Event id={}. Loading from Postgres.", id);
                    return eventJpaRepository.findById(id)
                            .map(entity -> {
                                EventDto dto = eventMapper.toDto(entity);
                                eventRedisRepository.save(eventMapper.toRedisEntity(dto));
                                log.info("Event id={} cached after loading from Postgres", id);
                                return dto;
                            })
                            .orElseThrow(() -> new RuntimeException("Event not found with id=" + id));
                });
    }


    public EventDto update(String id, EventDto dto) {
        log.info("Updating Event id={} in Postgres and Redis", id);
        dto = new EventDto(id, dto.title(), dto.description());
        var updated = eventJpaRepository.save(eventMapper.toJpaEntity(dto));
        eventRedisRepository.save(eventMapper.toRedisEntity(dto));
        return eventMapper.toDto(updated);
    }

    public void delete(String id) {
        log.info("Deleting Event id={} from Postgres and Redis", id);
        eventJpaRepository.deleteById(id);
        eventRedisRepository.deleteById(id);
    }
}
