package net.proselyte.api.mapper;

import net.proselyte.api.dto.UserDto;
import net.proselyte.api.entity.UserJpaEntity;
import net.proselyte.api.entity.UserRedisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Redis
    UserDto toDto(UserRedisEntity user);
    UserRedisEntity toRedisEntity(UserDto dto);

    // JPA
    UserDto toDto(UserJpaEntity user);
    UserJpaEntity toJpaEntity(UserDto dto);
}
