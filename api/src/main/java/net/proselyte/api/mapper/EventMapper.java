package net.proselyte.api.mapper;

import net.proselyte.api.dto.EventDto;
import net.proselyte.api.entity.EventJpaEntity;
import net.proselyte.api.entity.EventRedisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    // Redis
    EventDto toDto(EventRedisEntity event);
    EventRedisEntity toRedisEntity(EventDto dto);

    // JPA
    EventDto toDto(EventJpaEntity event);
    EventJpaEntity toJpaEntity(EventDto dto);
}
