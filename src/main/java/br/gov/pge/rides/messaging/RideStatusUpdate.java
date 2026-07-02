package br.gov.pge.rides.messaging;

import br.gov.pge.rides.model.enums.RideStatus;

public record RideStatusUpdate(
        Long rideId,
        Long userId,
        RideStatus status,
        Long driverId
) {
}
