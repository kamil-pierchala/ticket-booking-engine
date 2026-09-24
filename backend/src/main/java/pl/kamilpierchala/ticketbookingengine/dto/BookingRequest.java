package pl.kamilpierchala.ticketbookingengine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull(message = "Seat ID is required")
        Long seatId,

        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String userEmail
) {
}