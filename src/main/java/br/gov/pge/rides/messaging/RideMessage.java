package br.gov.pge.rides.messaging;

public record RideMessage(
        Long rideId,
        Long userId,
        String origin,
        String destination
) {
}
