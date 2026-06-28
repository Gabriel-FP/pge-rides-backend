package br.gov.pge.rides.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RideRequestDTO(

        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "origin is required")
        String origin,

        @NotBlank(message = "destination is required")
        String destination
) {
}
