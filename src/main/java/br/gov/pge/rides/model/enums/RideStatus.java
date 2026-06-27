package br.gov.pge.rides.model.enums;

public enum RideStatus {
    WAITING,      // created, waiting for a driver to accept
    IN_PROGRESS,  // a driver accepted
    CANCELLED     // nobody accepted or it was cancelled
}
