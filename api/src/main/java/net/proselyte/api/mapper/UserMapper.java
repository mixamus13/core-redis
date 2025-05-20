package net.proselyte.api.mapper;

import net.proselyte.api.dto.UserDto;
import net.proselyte.api.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto dto);
}
