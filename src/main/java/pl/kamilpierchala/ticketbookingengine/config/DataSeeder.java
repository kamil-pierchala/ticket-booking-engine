package pl.kamilpierchala.ticketbookingengine.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import pl.kamilpierchala.ticketbookingengine.domain.Event;
import pl.kamilpierchala.ticketbookingengine.domain.Seat;
import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;
import pl.kamilpierchala.ticketbookingengine.repository.EventRepository;
import pl.kamilpierchala.ticketbookingengine.repository.SeatRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data population.");
            return;
        }

        log.info("Seeding initial event and seats data...");

        Event concert = Event.builder()
                .name("Rock Festival 2026")
                .eventDate(LocalDateTime.now().plusMonths(2))
                .build();

        concert = eventRepository.save(concert);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seats.add(Seat.builder()
                    .seatNumber("A" + i)
                    .price(new BigDecimal("199.99"))
                    .status(SeatStatus.AVAILABLE)
                    .event(concert)
                    .build());
        }

        seatRepository.saveAll(seats);
        log.info("Successfully seeded event '{}' with {} available seats.", concert.getName(), seats.size());
    }
}