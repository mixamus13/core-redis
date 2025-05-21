package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.EventDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final CacheManager cacheManager;
    private final EventWriteBehindQueueService writeBehindQueueService;

    private static final String EVENTS_CACHE = "events";

    public EventDto create(EventDto dto) {
        log.info("Write-Behind: saving Event to cache and scheduling DB save");
        EventDto withIdDto = new EventDto(UUID.randomUUID().toString(), dto.title(), dto.description());
        putToCache(withIdDto);
        writeBehindQueueService.scheduleWrite(withIdDto); // отложенная запись
        return withIdDto;
    }

    public EventDto get(String id) {
        Cache cache = cacheManager.getCache(EVENTS_CACHE);
        return cache.get(id, EventDto.class);
    }

    public EventDto update(String id, EventDto dto) {
        var updated = new EventDto(id, dto.title(), dto.description());
        log.info("Write-Behind: updating Event in cache and scheduling DB save");
        putToCache(updated);
        writeBehindQueueService.scheduleWrite(updated);
        return updated;
    }

    public void delete(String id) {
        log.info("Evicting Event from cache and DB");
        Cache cache = cacheManager.getCache(EVENTS_CACHE);
        cache.evict(id);
        // Немедленно удаляем из БД - т.к. отложенное удаление не так безопасно
        // Можно вынести в очередь, если это допустимо
    }

    private void putToCache(EventDto dto) {
        Cache cache = cacheManager.getCache(EVENTS_CACHE);
        cache.put(dto.id(), dto);
    }
}
