package net.proselyte.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.api.dto.EventDto;
import net.proselyte.api.mapper.EventMapper;
import net.proselyte.api.repository.EventRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventWriteBehindQueueService {
    private final EventRepository eventJpaRepository;
    private final EventMapper eventMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public void scheduleWrite(EventDto dto) {
        executor.submit(() -> {
            try {
                Thread.sleep(15_000); // Задержка 15 секунд (в миллисекундах)
                log.info("Async DB save for Event id={}", dto.id());
                eventJpaRepository.save(eventMapper.toJpaEntity(dto));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Async write task was interrupted", e);
            } catch (Exception e) {
                log.error("Failed to save Event to DB", e);
            }
        });
    }


}

