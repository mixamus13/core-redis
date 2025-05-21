package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.UserDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final CacheManager cacheManager;
    private final UserWriteBehindQueueService writeBehindQueueService;
    private final RedisTemplate<String, Object> redisTemplate;


    private static final String USERS_CACHE = "users";

    public UserDto create(UserDto dto) {
        log.info("Write-Behind: saving User to cache and scheduling DB save");
        UserDto withIdDto = new UserDto(UUID.randomUUID().toString(), dto.name(), dto.age(), dto.events());
        putToCache(withIdDto);
        writeBehindQueueService.scheduleWrite(withIdDto); // отложенная запись
        return withIdDto;
    }

    public UserDto get(String id) {
        Cache cache = cacheManager.getCache(USERS_CACHE);
        return cache.get(id, UserDto.class);
    }

    public List<UserDto> getAll() {
        String pattern = USERS_CACHE + "::*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) return List.of();

        List<Object> rawValues = redisTemplate.opsForValue().multiGet(keys);
        return rawValues.stream()
                .filter(UserDto.class::isInstance)
                .map(UserDto.class::cast)
                .toList();
    }

    public UserDto update(String id, UserDto dto) {
        var updated = new UserDto(id, dto.name(), dto.age(), dto.events());
        log.info("Write-Behind: updating User in cache and scheduling DB save");
        putToCache(updated);
        writeBehindQueueService.scheduleWrite(updated);
        return updated;
    }

    public void delete(String id) {
        log.info("Evicting User from cache and DB");
        Cache cache = cacheManager.getCache(USERS_CACHE);
        cache.evict(id);
        // Немедленно удаляем из БД - т.к. отложенное удаление не так безопасно
        // Можно вынести в очередь, если это допустимо
    }

    private void putToCache(UserDto dto) {
        Cache cache = cacheManager.getCache(USERS_CACHE);
        cache.put(dto.id(), dto);
    }
}
