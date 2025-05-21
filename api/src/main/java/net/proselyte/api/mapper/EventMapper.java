package net.proselyte.api.mapper;

import net.proselyte.api.dto.EventDto;
import net.proselyte.api.entity.EventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto toDto(EventEntity event);

    EventEntity toJpaEntity(EventDto dto);
}
