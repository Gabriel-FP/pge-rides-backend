package br.gov.pge.rides.repository;

import br.gov.pge.rides.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {
}
