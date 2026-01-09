package com.mixamus.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.mixamus.api.dto.UserDto;
import com.mixamus.api.mapper.UserMapper;
import com.mixamus.api.repository.UserRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final RedisClient redisClient;

    @CachePut(cacheNames = "users", key = "#result.id")
    public UserDto create(UserDto dto) {
        log.info("Saving User to Postgres and cache");
        var dtoWithId = new UserDto(UUID.randomUUID().toString(), dto.name(), dto.age(), dto.events());
        var saved = userRepository.save(userMapper.toJpaEntity(dtoWithId));
        return userMapper.toDto(saved);
    }

    @Cacheable(cacheNames = "users", key = "#id", unless = "#result == null")
    public UserDto get(String id) {
        log.info("Cache miss — loading User id={} from Postgres", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
    }

    @CachePut(cacheNames = "users", key = "#id")
    public UserDto update(String id, UserDto dto) {
        log.info("Updating User id={} in Postgres and refreshing cache", id);
        dto = new UserDto(id, dto.name(), dto.age(), dto.events());
        var updated = userRepository.save(userMapper.toJpaEntity(dto));
        return userMapper.toDto(updated);
    }

    @CacheEvict(cacheNames = "users", key = "#id")
    public void delete(String id) {
        log.info("Deleting User id={} from Postgres and evicting cache", id);
        userRepository.deleteById(id);
    }
    public void incrementVisitUnsafe(String userId) {
        String key = "user:visits:" + userId;
        String current = redisTemplate.opsForValue().get(key);

        int count = 0;
        try {
            count = Integer.parseInt(current);
        } catch (NumberFormatException e) {
            log.warn("Corrupted counter for {} → '{}', resetting to 0", userId, current);
        }

        count++;
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        log.info("Unsafe increment for {} → {}", userId, count);
    }

    public void incrementWithWatch(String userId) {
        try (StatefulRedisConnection<String, String> conn = redisClient.connect()) {
            RedisCommands<String, String> sync = conn.sync();
            String key = "user:visits:" + userId;

            while (true) {
                try {
                    sync.watch(key);
                    String val = sync.get(key);
                    int newVal = val == null ? 1 : Integer.parseInt(val) + 1;

                    sync.multi();
                    sync.set(key, String.valueOf(newVal));
                    TransactionResult result = sync.exec();

                    if (result.wasDiscarded()) {
                        log.warn("WATCH conflict for key {}, retrying...", key);
                        continue;
                    }

                    log.info("WATCH: {} = {}", key, newVal);
                    break;
                } catch (Exception e) {
                    log.error("WATCH error for {}: {}", key, e.getMessage());
                    break;
                } finally {
                    sync.unwatch();
                }
            }
        }
    }

    public void incrementWithLock(String userId) {
        String key = "user:visits:" + userId;
        RLock lock = redissonClient.getLock("lock:" + key);
        try {
            lock.lock();
            String val = redisTemplate.opsForValue().get(key);
            int newVal = Integer.parseInt(val) + 1;
            redisTemplate.opsForValue().set(key, String.valueOf(newVal));
            log.info("LOCK: {} = {}", key, newVal);
        } finally {
            lock.unlock();
        }
    }

    public void incrementVisitWithIncr(String userId) {
        String key = "user:visits:" + userId;
        Long newValue = redisTemplate.opsForValue().increment(key);
        log.info("INCR → {} = {}", key, newValue);
    }

    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @SneakyThrows
    public void saveUserToRedisJson(UserDto user) {
        String key = "user:json:" + user.id();
        redisTemplate.getConnectionFactory().getConnection()
                .execute("JSON.SET", key.getBytes(), "$".getBytes(),
                        new ObjectMapper().writeValueAsBytes(user));
    }

    @SneakyThrows
    public UserDto getUserFromRedisJson(String id) {
        String key = "user:json:" + id;
        byte[] raw = (byte[]) redisTemplate.getConnectionFactory()
                .getConnection()
                .execute("JSON.GET", key.getBytes());

        return new ObjectMapper().readValue(raw, UserDto.class);
    }

}
