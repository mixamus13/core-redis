package net.proselyte.api.rest;

import net.proselyte.api.dto.EventDto;
import net.proselyte.api.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventRestControllerV1 {
    private final EventService eventService;

    @PostMapping
    public EventDto create(@RequestBody EventDto dto) {
        return eventService.create(dto);
    }

    @GetMapping("/{id}")
    public EventDto get(@PathVariable String id) {
        return eventService.get(id);
    }

    @PutMapping("/{id}")
    public EventDto update(@PathVariable String id, @RequestBody EventDto dto) {
        return eventService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        eventService.delete(id);
    }
}
