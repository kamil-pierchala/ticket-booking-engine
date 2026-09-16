package pl.kamilpierchala.ticketbookingengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamilpierchala.ticketbookingengine.domain.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Finds unpaid reservations that have already expired
    List<Reservation> findByPaidFalseAndExpiresAtBefore(LocalDateTime now);
}