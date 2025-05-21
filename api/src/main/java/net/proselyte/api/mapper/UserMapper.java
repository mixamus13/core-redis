package net.proselyte.api.mapper;

import net.proselyte.api.dto.UserDto;
import net.proselyte.api.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity user);

    UserEntity toJpaEntity(UserDto dto);
}
