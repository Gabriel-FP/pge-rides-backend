package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideMessage;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DriverNotificationService {

    private static final Logger log = LoggerFactory.getLogger(DriverNotificationService.class);

    // One open SSE connection per logged-in driver, kept in memory.
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final RideRepository repository;

    public DriverNotificationService(RideRepository repository) {
        this.repository = repository;
    }

    public SseEmitter subscribe(Long driverId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // never times out on its own

        // Clean up the registry whenever the connection ends.
        emitter.onCompletion(() -> emitters.remove(driverId));
        emitter.onTimeout(() -> emitters.remove(driverId));
        emitter.onError(e -> emitters.remove(driverId));

        emitters.put(driverId, emitter);
        log.info("Driver {} subscribed to notifications ({} connected)", driverId, emitters.size());

        // Catch this driver up on rides that were created before they connected
        // (e.g. created while no driver was online) — otherwise they sit invisible
        // until the timeout scheduler cancels them.
        replayWaitingRides(driverId, emitter);
        return emitter;
    }

    private void replayWaitingRides(Long driverId, SseEmitter emitter) {
        List<Ride> waiting = repository.findByStatus(RideStatus.WAITING);
        for (Ride ride : waiting) {
            try {
                emitter.send(SseEmitter.event()
                        .name("new-ride")
                        .data(new RideMessage(ride.getId(), ride.getUserId(), ride.getOrigin(), ride.getDestination())));
            } catch (IOException ex) {
                log.error("Failed to replay ride {} to driver {}", ride.getId(), driverId, ex);
                emitters.remove(driverId);
                break;
            }
        }
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
