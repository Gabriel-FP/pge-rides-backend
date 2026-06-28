package br.gov.pge.rides.scheduler;

import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RideTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(RideTimeoutScheduler.class);

    private final RideRepository repository;
    private final long timeoutSeconds;

    public RideTimeoutScheduler(RideRepository repository,
                                @Value("${rides.timeout-seconds:60}") long timeoutSeconds) {
        this.repository = repository;
        this.timeoutSeconds = timeoutSeconds;
    }

    // Runs on a fixed schedule: any ride still WAITING past the timeout is cancelled.
    @Scheduled(fixedDelayString = "${rides.timeout-check-interval-ms:15000}")
    public void cancelExpiredRides() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(timeoutSeconds);
        List<Ride> expired = repository.findByStatusAndCreatedAtBefore(RideStatus.WAITING, threshold);

        for (Ride ride : expired) {
            ride.setStatus(RideStatus.CANCELLED);
            repository.save(ride);
            log.warn("Ride {} cancelled: no driver accepted within {}s", ride.getId(), timeoutSeconds);
        }
    }
}
