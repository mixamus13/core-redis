package net.proselyte.api.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("Event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRedisEntity implements Serializable {
    @Id
    private String id;
    private String title;
    private String description;
}
