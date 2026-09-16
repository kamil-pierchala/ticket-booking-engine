package pl.kamilpierchala.ticketbookingengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kamilpierchala.ticketbookingengine.domain.Reservation;
import pl.kamilpierchala.ticketbookingengine.domain.Seat;
import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;
import pl.kamilpierchala.ticketbookingengine.dto.BookingRequest;
import pl.kamilpierchala.ticketbookingengine.dto.BookingResponse;
import pl.kamilpierchala.ticketbookingengine.exception.ResourceNotFoundException;
import pl.kamilpierchala.ticketbookingengine.exception.SeatAlreadyBookedException;
import pl.kamilpierchala.ticketbookingengine.repository.ReservationRepository;
import pl.kamilpierchala.ticketbookingengine.repository.SeatRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    private static final int RESERVATION_HOLD_MINUTES = 10;

    @Transactional
    public BookingResponse lockAndReserveSeat(BookingRequest request) {
        log.info("Attempting to reserve seat ID: {} for user: {}", request.seatId(), request.userEmail());

        // Get the location with pessimistic lock
        Seat seat = seatRepository.findByIdWithPessimisticLock(request.seatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + request.seatId()));

        // Check if the place is available
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new SeatAlreadyBookedException(
                    "Seat " + seat.getSeatNumber() + " is already " + seat.getStatus()
            );
        }

        // Changing the status of a place to temporarily blocked
        seat.setStatus(SeatStatus.LOCKED);
        seatRepository.save(seat);

        // Creating a reservation with an expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESERVATION_HOLD_MINUTES);
        Reservation reservation = Reservation.builder()
                .userEmail(request.userEmail())
                .seat(seat)
                .expiresAt(expiresAt)
                .paid(false)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Seat ID: {} successfully locked until: {}", seat.getId(), expiresAt);

        return new BookingResponse(
                savedReservation.getId(),
                seat.getId(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus(),
                savedReservation.getExpiresAt(),
                savedReservation.getUserEmail()
        );
    }

    @Transactional
    public void confirmPayment(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Reservation has expired. Cannot confirm payment.");
        }

        reservation.setPaid(true);
        reservation.getSeat().setStatus(SeatStatus.BOOKED);
        log.info("Payment confirmed for reservation ID: {}, seat is now BOOKED", reservationId);
    }
}