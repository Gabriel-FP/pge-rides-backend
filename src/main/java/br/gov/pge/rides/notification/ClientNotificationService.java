package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideStatusUpdate;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ClientNotificationService.class);

    private static final List<RideStatus> ACTIVE_STATUSES =
            List.of(RideStatus.WAITING, RideStatus.IN_PROGRESS);

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RideRepository repository;

    public ClientNotificationService(RideRepository repository) {
        this.repository = repository;
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        emitters.put(userId, emitter);
        log.info("User {} subscribed to notifications ({} connected)", userId, emitters.size());

        replayActiveRide(userId, emitter);
        return emitter;
    }

    private void replayActiveRide(Long userId, SseEmitter emitter) {
        Optional<Ride> active = repository.findFirstByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        active.ifPresent(ride -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ride-status")
                        .data(new RideStatusUpdate(ride.getId(), ride.getUserId(), ride.getStatus(), ride.getDriverId())));
            } catch (IOException ex) {
                log.error("Failed to replay ride {} status to user {}", ride.getId(), userId, ex);
                emitters.remove(userId);
            }
        });
    }

    public void notifyStatusChange(RideStatusUpdate update) {
        SseEmitter emitter = emitters.get(update.userId());
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("ride-status")
                    .data(update));
        } catch (IOException ex) {
            log.error("Failed to notify user {} about ride {} status change", update.userId(), update.rideId(), ex);
            emitters.remove(update.userId());
        }
    }
}
