package net.proselyte.api.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@RedisHash("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    private String id;
    private String name;
    private int age;
    @Builder.Default
    private Set<String> events = new HashSet<>();
}
