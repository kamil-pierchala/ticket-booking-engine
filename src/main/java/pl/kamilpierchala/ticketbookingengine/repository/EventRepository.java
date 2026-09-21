package pl.kamilpierchala.ticketbookingengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamilpierchala.ticketbookingengine.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}