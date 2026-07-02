package br.gov.pge.rides.dto;

import br.gov.pge.rides.model.enums.CancelledBy;
import br.gov.pge.rides.model.enums.RideStatus;

import java.time.LocalDateTime;

public record RideResponseDTO(
        Long id,
        Long userId,
        String origin,
        String destination,
        RideStatus status,
        Long driverId,
        LocalDateTime createdAt,
        CancelledBy cancelledBy
) {
}
