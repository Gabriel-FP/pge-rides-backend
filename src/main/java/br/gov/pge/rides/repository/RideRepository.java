package br.gov.pge.rides.repository;

import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    // Spring Data derives the query from the method name:
    // SELECT count(*) > 0 ... WHERE user_id = ? AND status IN (?)
    boolean existsByUserIdAndStatusIn(Long userId, Collection<RideStatus> statuses);

    // Used by the timeout scheduler to find rides nobody accepted in time.
    List<Ride> findByStatusAndCreatedAtBefore(RideStatus status, LocalDateTime threshold);
}
