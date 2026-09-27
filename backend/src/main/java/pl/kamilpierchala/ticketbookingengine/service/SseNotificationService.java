package pl.kamilpierchala.ticketbookingengine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.kamilpierchala.ticketbookingengine.dto.SeatResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseNotificationService {

    // thread-safe list to hold active client connections
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // 30 minutes timeout
        SseEmitter emitter = new SseEmitter(1800_000L);

        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError(e -> this.emitters.remove(emitter));

        // initial handshake event
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to Real-Time Seat Updates"));
        } catch (IOException e) {
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcastSeatUpdate(SeatResponse seat) {
        log.info("Broadcasting live status update for seat: {} -> {}", seat.seatNumber(), seat.status());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("SEAT_UPDATED")
                        .data(seat));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}