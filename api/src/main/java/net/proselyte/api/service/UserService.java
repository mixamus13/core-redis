package net.proselyte.api.service;

import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.UserDto;
import net.proselyte.api.mapper.UserMapper;
import net.proselyte.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto dto) {
        UserDto withIdDto = new UserDto(UUID.randomUUID().toString(), dto.name(), dto.age(), dto.events());
        var saved = userRepository.save(userMapper.toEntity(withIdDto));
        log.info("IN create - saved user: {}", saved);
        return userMapper.toDto(saved);
    }

    public UserDto get(String id) {
        return userRepository.findById(id).map(userMapper::toDto).orElse(null);
    }

    public UserDto update(String id, UserDto dto) {
        var entity = userMapper.toEntity(dto);
        entity.setId(id);
        return userMapper.toDto(userRepository.save(entity));
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
