package com.mixamus.api.mapper;

import com.mixamus.api.dto.UserDto;
import com.mixamus.api.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(UserEntity user);

    UserEntity toJpaEntity(UserDto dto);
}
