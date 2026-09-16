package pl.kamilpierchala.ticketbookingengine.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kamilpierchala.ticketbookingengine.domain.Seat;
import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);

    // Prevents a situation where two threads read the same free space within a fraction of a second
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithPessimisticLock(@Param("id") Long id);
}