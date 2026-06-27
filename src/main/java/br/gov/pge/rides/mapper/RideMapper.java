package br.gov.pge.rides.mapper;

import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.model.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    public Ride toEntity(RideRequestDTO dto) {
        Ride ride = new Ride();
        ride.setUserId(dto.userId());
        ride.setOrigin(dto.origin());
        ride.setDestination(dto.destination());
        return ride;
    }

    public RideResponseDTO toResponse(Ride ride) {
        return new RideResponseDTO(
                ride.getId(),
                ride.getUserId(),
                ride.getOrigin(),
                ride.getDestination(),
                ride.getStatus(),
                ride.getDriverId(),
                ride.getCreatedAt()
        );
    }
}
