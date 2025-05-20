package net.proselyte.api.mapper;

import net.proselyte.api.dto.EventDto;
import net.proselyte.api.entity.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventDto toDto(Event event);
    Event toEntity(EventDto dto);
}
