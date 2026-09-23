package pl.kamilpierchala.ticketbookingengine.dto;

import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;

import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        String seatNumber,
        BigDecimal price,
        SeatStatus status
) {
}