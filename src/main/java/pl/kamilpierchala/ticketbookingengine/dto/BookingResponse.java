package pl.kamilpierchala.ticketbookingengine.dto;

import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long reservationId,
        Long seatId,
        String seatNumber,
        BigDecimal price,
        SeatStatus status,
        LocalDateTime expiresAt,
        String userEmail
) {
}