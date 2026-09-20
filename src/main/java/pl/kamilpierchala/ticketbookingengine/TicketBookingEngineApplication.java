package pl.kamilpierchala.ticketbookingengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketBookingEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketBookingEngineApplication.class, args);
	}

}
