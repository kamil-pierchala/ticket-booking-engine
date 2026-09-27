package pl.kamilpierchala.ticketbookingengine.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kamilpierchala.ticketbookingengine.dto.BookingRequest;
import pl.kamilpierchala.ticketbookingengine.dto.BookingResponse;
import pl.kamilpierchala.ticketbookingengine.dto.SeatResponse;
import pl.kamilpierchala.ticketbookingengine.service.BookingService;
import pl.kamilpierchala.ticketbookingengine.service.SseNotificationService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SseNotificationService sseNotificationService;

    // POST /api/v1/booking/reserve
    @PostMapping("/reserve")
    public ResponseEntity<BookingResponse> reserveSeat(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.lockAndReserveSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/bookings/{reservationId}/confirm-payment
    @PostMapping("/{reservationId}/confirm-payment")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long reservationId) {
        bookingService.confirmPayment(reservationId);
        return ResponseEntity.ok().build();
    }

    // GET /api/v1/bookings/events/{eventId}/seats
    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getAllSeats(@PathVariable Long eventId) {
        List<SeatResponse> seats = bookingService.getAllSeatsForEvent(eventId);
        return ResponseEntity.ok(seats);
    }

    // GET /api/v1/bookings/stream
    @GetMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSeatUpdates() {
        return sseNotificationService.subscribe();
    }
}
