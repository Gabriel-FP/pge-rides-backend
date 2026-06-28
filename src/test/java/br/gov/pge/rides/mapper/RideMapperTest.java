package br.gov.pge.rides.mapper;

import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RideMapperTest {

    private final RideMapper mapper = new RideMapper();

    @Test
    void toEntity_shouldMapAllFieldsFromRequest() {
        RideRequestDTO request = new RideRequestDTO(5L, "Centro", "Aeroporto");

        Ride entity = mapper.toEntity(request);

        assertEquals(5L, entity.getUserId());
        assertEquals("Centro", entity.getOrigin());
        assertEquals("Aeroporto", entity.getDestination());
        assertNull(entity.getId());
        assertNull(entity.getStatus());
        assertNull(entity.getDriverId());
    }

    @Test
    void toResponse_shouldMapAllFieldsFromEntity() {
        Ride ride = new Ride();
        ride.setId(10L);
        ride.setUserId(3L);
        ride.setOrigin("Praia");
        ride.setDestination("Shopping");
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setDriverId(7L);
        ride.setCreatedAt(LocalDateTime.of(2026, 6, 28, 14, 30));

        RideResponseDTO response = mapper.toResponse(ride);

        assertEquals(10L, response.id());
        assertEquals(3L, response.userId());
        assertEquals("Praia", response.origin());
        assertEquals("Shopping", response.destination());
        assertEquals(RideStatus.IN_PROGRESS, response.status());
        assertEquals(7L, response.driverId());
        assertEquals(LocalDateTime.of(2026, 6, 28, 14, 30), response.createdAt());
    }

    @Test
    void toResponse_shouldHandleNullDriverId() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setUserId(1L);
        ride.setOrigin("A");
        ride.setDestination("B");
        ride.setStatus(RideStatus.WAITING);
        ride.setCreatedAt(LocalDateTime.now());

        RideResponseDTO response = mapper.toResponse(ride);

        assertNull(response.driverId());
    }
}
