package br.gov.pge.rides.exception;

public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(Long rideId) {
        super("Ride " + rideId + " not found");
    }
}
