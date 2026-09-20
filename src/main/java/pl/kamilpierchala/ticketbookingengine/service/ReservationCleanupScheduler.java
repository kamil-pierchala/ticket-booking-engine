package pl.kamilpierchala.ticketbookingengine.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.kamilpierchala.ticketbookingengine.domain.Reservation;
import pl.kamilpierchala.ticketbookingengine.domain.Seat;
import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;
import pl.kamilpierchala.ticketbookingengine.repository.ReservationRepository;
import pl.kamilpierchala.ticketbookingengine.repository.SeatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupScheduler {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    // runs every 30 seconds, after previous execution
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void releaseExpiredReservation() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findByPaidFalseAndExpiresAtBefore(now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        log.info("Found {} expired reservation(s). Releasing seats.", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            Seat seat = reservation.getSeat();

            // only release seat if it's still in the LOCKED state
            if (seat.getStatus() == SeatStatus.LOCKED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
                log.info("Released seat ID: {} back to AVAILABLE", seat.getId());
            }
            reservationRepository.delete(reservation);
        }
        log.info("Finished releasing expired reservations");
    }
}
