package br.gov.pge.rides.exception;

public class RideNotAvailableException extends RuntimeException {

    public RideNotAvailableException(Long rideId) {
        super("Ride " + rideId + " is not available to be accepted");
    }
}
