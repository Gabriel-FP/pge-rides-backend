package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DriverNotificationService {

    private static final Logger log = LoggerFactory.getLogger(DriverNotificationService.class);

    // One open SSE connection per logged-in driver, kept in memory.
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long driverId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // never times out on its own

        // Clean up the registry whenever the connection ends.
        emitter.onCompletion(() -> emitters.remove(driverId));
        emitter.onTimeout(() -> emitters.remove(driverId));
        emitter.onError(e -> emitters.remove(driverId));

        emitters.put(driverId, emitter);
        log.info("Driver {} subscribed to notifications ({} connected)", driverId, emitters.size());
        return emitter;
    }

    public void notifyNewRide(RideMessage ride) {
        emitters.forEach((driverId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-ride")
                        .data(ride));
            } catch (IOException ex) {
                log.error("Failed to notify driver {} about ride {}", driverId, ride.rideId(), ex);
                emitters.remove(driverId);
            }
        });
        log.info("Ride {} broadcast to {} connected driver(s)", ride.rideId(), emitters.size());
    }
}
