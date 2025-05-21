package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.EventDto;
import net.proselyte.api.entity.EventEntity;
import net.proselyte.api.mapper.EventMapper;
import net.proselyte.api.repository.EventRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventDto create(EventDto dto) {
        log.info("Saving Event to Postgres and Redis");
        EventDto withIdDto = new EventDto(UUID.randomUUID().toString(), dto.title(), dto.description());
        EventEntity saved = eventRepository.save(eventMapper.toJpaEntity(withIdDto));
        return eventMapper.toDto(saved);
    }

    @Cacheable(cacheNames = "events", key = "#id", unless = "#result == null")
    public EventDto get(String id) {
        log.info("Cache miss — loading Event id={} from Postgres", id);
        return eventRepository.findById(id).map(eventMapper::toDto).orElse(null);
    }

    @CachePut(cacheNames = "events", key = "#id")
    public EventDto update(String id, EventDto dto) {
        log.info("Updating Event id={} in Postgres and refreshing cache", id);
        dto = new EventDto(id, dto.title(), dto.description());
        var updated = eventRepository.save(eventMapper.toJpaEntity(dto));
        return eventMapper.toDto(updated);
    }

    @CacheEvict(cacheNames = "events", key = "#id")
    public void delete(String id) {
        log.info("Deleting Event id={} from Postgres and evicting cache", id);
        eventRepository.deleteById(id);
    }
}
