package net.proselyte.api.rest;

import net.proselyte.api.dto.UserDto;
import net.proselyte.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestControllerV1 {
    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto user) {
        return userService.create(user);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable String id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable String id, @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}
