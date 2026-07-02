package br.gov.pge.rides.repository;

import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    // Spring Data derives the query from the method name:
    // SELECT count(*) > 0 ... WHERE user_id = ? AND status IN (?)
    boolean existsByUserIdAndStatusIn(Long userId, Collection<RideStatus> statuses);

    // Used by the timeout scheduler to find rides nobody accepted in time.
    List<Ride> findByStatusAndCreatedAtBefore(RideStatus status, LocalDateTime threshold);

    // Used to catch up a driver who connects to SSE after rides were already created.
    List<Ride> findByStatus(RideStatus status);

    // Prevents a driver from accepting two rides simultaneously.
    boolean existsByDriverIdAndStatus(Long driverId, RideStatus status);

    // Used by ClientNotificationService to replay the active ride on subscribe.
    Optional<Ride> findFirstByUserIdAndStatusIn(Long userId, Collection<RideStatus> statuses);
}
