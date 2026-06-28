package br.gov.pge.rides.exception;

public class ActiveRideAlreadyExistsException extends RuntimeException {

    public ActiveRideAlreadyExistsException(Long userId) {
        super("User " + userId + " already has an active ride");
    }
}
