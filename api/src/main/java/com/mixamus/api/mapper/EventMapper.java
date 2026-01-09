package com.mixamus.api.mapper;

import com.mixamus.api.dto.EventDto;
import com.mixamus.api.entity.EventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto toDto(EventEntity event);

    EventEntity toJpaEntity(EventDto dto);
}
