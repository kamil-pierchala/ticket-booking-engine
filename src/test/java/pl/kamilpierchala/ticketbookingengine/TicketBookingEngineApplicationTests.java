package pl.kamilpierchala.ticketbookingengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.kamilpierchala.ticketbookingengine.domain.Event;
import pl.kamilpierchala.ticketbookingengine.domain.Seat;
import pl.kamilpierchala.ticketbookingengine.domain.SeatStatus;
import pl.kamilpierchala.ticketbookingengine.dto.BookingRequest;
import pl.kamilpierchala.ticketbookingengine.dto.BookingResponse;
import pl.kamilpierchala.ticketbookingengine.exception.SeatAlreadyBookedException;
import pl.kamilpierchala.ticketbookingengine.repository.EventRepository;
import pl.kamilpierchala.ticketbookingengine.repository.ReservationRepository;
import pl.kamilpierchala.ticketbookingengine.repository.SeatRepository;
import pl.kamilpierchala.ticketbookingengine.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingConcurrencyTest {

	@Autowired
	private BookingService bookingService;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	@DisplayName("Should allow only one user to book a seat during high-concurrency race condition")
	void shouldAllowOnlyOneUserToBookSeatUnderHighConcurrency() throws InterruptedException {
		// Preparation of the event and ONE available spot
		Event event = eventRepository.save(Event.builder()
				.name("Sold Out Arena Tour")
				.eventDate(LocalDateTime.now().plusWeeks(1))
				.build());

		Seat targetSeat = seatRepository.save(Seat.builder()
				.seatNumber("VIP-1")
				.price(new BigDecimal("499.99"))
				.status(SeatStatus.AVAILABLE)
				.event(event)
				.build());

		int numberOfThreads = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

		// Synchronizes the start of all threads at exactly the same moment
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

		AtomicInteger successfulReservations = new AtomicInteger(0);
		AtomicInteger rejectedReservations = new AtomicInteger(0);

		// Simulation of 10 users clicking "reserve" at the exact same fraction of a second
		for (int i = 0; i < numberOfThreads; i++) {
			final String userEmail = "user" + i + "@example.com";
			executorService.submit(() -> {
				try {
					startLatch.await(); // Wait for the start signal
					BookingResponse response = bookingService.lockAndReserveSeat(
							new BookingRequest(targetSeat.getId(), userEmail)
					);
					if (response != null) {
						successfulReservations.incrementAndGet();
					}
				} catch (SeatAlreadyBookedException e) {
					rejectedReservations.incrementAndGet();
				} catch (Exception e) {
					// Other unexpected errors
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown(); // releases all 10 threads simultaneously
		endLatch.await();       // wait for all threads to finish their work.
		executorService.shutdown();

		// Verification of concurrency results
		assertThat(successfulReservations.get())
				.as("Only 1 reservation must succeed under concurrency")
				.isEqualTo(1);

		assertThat(rejectedReservations.get())
				.as("All other 9 threads must receive SeatAlreadyBookedException")
				.isEqualTo(9);

		Seat updatedSeat = seatRepository.findById(targetSeat.getId()).orElseThrow();
		assertThat(updatedSeat.getStatus())
				.as("Seat status in database must be LOCKED")
				.isEqualTo(SeatStatus.LOCKED);
	}
}