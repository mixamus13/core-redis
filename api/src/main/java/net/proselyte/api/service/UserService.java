package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.UserDto;
import net.proselyte.api.mapper.UserMapper;
import net.proselyte.api.repository.UserJpaRepository;
import net.proselyte.api.repository.UserRedisRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userJpaRepository;
    private final UserRedisRepository userRedisRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto dto) {
        log.info("Saving User to Postgres and Redis");
        UserDto withIdDto = new UserDto(UUID.randomUUID().toString(), dto.name(), dto.age(), dto.events());
        var saved = userJpaRepository.save(userMapper.toJpaEntity(withIdDto));
        userRedisRepository.save(userMapper.toRedisEntity(withIdDto));
        return userMapper.toDto(saved);
    }

    public UserDto get(String id) {
        log.info("Trying to get User from Redis with id={}", id);

        return userRedisRepository.findById(id)
                .map(cached -> {
                    log.info("Cache hit for User id={}", id);
                    return userMapper.toDto(cached);
                })
                .orElseGet(() -> {
                    log.info("Cache miss for User id={}. Loading from Postgres.", id);
                    return userJpaRepository.findById(id)
                            .map(entity -> {
                                UserDto dto = userMapper.toDto(entity);
                                userRedisRepository.save(userMapper.toRedisEntity(dto));
                                log.info("User id={} cached after loading from Postgres", id);
                                return dto;
                            })
                            .orElseThrow(() -> new RuntimeException("User not found with id=" + id));
                });
    }


    public UserDto update(String id, UserDto dto) {
        log.info("Updating User id={} in Postgres and Redis", id);
        dto = new UserDto(id, dto.name(), dto.age(), dto.events());
        var updated = userJpaRepository.save(userMapper.toJpaEntity(dto));
        userRedisRepository.save(userMapper.toRedisEntity(dto));
        return userMapper.toDto(updated);
    }

    public void delete(String id) {
        log.info("Deleting User id={} from Postgres and Redis", id);
        userJpaRepository.deleteById(id);
        userRedisRepository.deleteById(id);
    }
}
