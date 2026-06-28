package br.gov.pge.rides.service;

import br.gov.pge.rides.cache.RideCacheService;
import br.gov.pge.rides.dto.RideRequestDTO;
import br.gov.pge.rides.dto.RideResponseDTO;
import br.gov.pge.rides.exception.ActiveRideAlreadyExistsException;
import br.gov.pge.rides.exception.RideNotAvailableException;
import br.gov.pge.rides.exception.RideNotFoundException;
import br.gov.pge.rides.mapper.RideMapper;
import br.gov.pge.rides.messaging.RideProducer;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository repository;

    @Mock
    private RideMapper mapper;

    @Mock
    private RideProducer producer;

    @Mock
    private RideCacheService cache;

    @InjectMocks
    private RideService service;

    // --- create ---

    @Test
    void create_shouldSaveAndPublishRide() {
        RideRequestDTO request = new RideRequestDTO(1L, "Rua A", "Rua B");
        Ride entity = buildRide(10L, 1L, RideStatus.WAITING);
        RideResponseDTO response = buildResponse(10L, 1L, RideStatus.WAITING);

        when(repository.existsByUserIdAndStatusIn(eq(1L), any())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        RideResponseDTO result = service.create(request);

        assertEquals(10L, result.id());
        assertEquals(RideStatus.WAITING, result.status());
        verify(producer).publishRideCreated(any());
    }

    @Test
    void create_shouldThrowWhenUserAlreadyHasActiveRide() {
        RideRequestDTO request = new RideRequestDTO(1L, "Rua A", "Rua B");
        when(repository.existsByUserIdAndStatusIn(eq(1L), any())).thenReturn(true);

        assertThrows(ActiveRideAlreadyExistsException.class, () -> service.create(request));
        verify(repository, never()).save(any());
        verify(producer, never()).publishRideCreated(any());
    }

    // --- accept ---

    @Test
    void accept_shouldLinkDriverAndCacheRide() {
        Ride ride = buildRide(10L, 1L, RideStatus.WAITING);
        RideResponseDTO response = buildResponse(10L, 1L, RideStatus.IN_PROGRESS);

        when(repository.findById(10L)).thenReturn(Optional.of(ride));
        when(repository.save(ride)).thenReturn(ride);
        when(mapper.toResponse(ride)).thenReturn(response);

        RideResponseDTO result = service.accept(10L, 99L);

        assertEquals(RideStatus.IN_PROGRESS, result.status());
        assertEquals(99L, ride.getDriverId());
        assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
        verify(cache).save(response);
    }

    @Test
    void accept_shouldThrowWhenRideNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> service.accept(999L, 1L));
    }

    @Test
    void accept_shouldThrowWhenRideNotWaiting() {
        Ride ride = buildRide(10L, 1L, RideStatus.IN_PROGRESS);
        when(repository.findById(10L)).thenReturn(Optional.of(ride));

        assertThrows(RideNotAvailableException.class, () -> service.accept(10L, 1L));
        verify(repository, never()).save(any());
    }

    // --- getStatus ---

    @Test
    void getStatus_shouldReturnCachedRide() {
        RideResponseDTO response = buildResponse(10L, 1L, RideStatus.IN_PROGRESS);
        when(cache.find(10L)).thenReturn(Optional.of(response));

        RideResponseDTO result = service.getStatus(10L);

        assertEquals(10L, result.id());
    }

    @Test
    void getStatus_shouldThrowWhenNotInCache() {
        when(cache.find(10L)).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> service.getStatus(10L));
    }

    // --- findAll ---

    @Test
    void findAll_shouldReturnAllRides() {
        Ride r1 = buildRide(1L, 1L, RideStatus.WAITING);
        Ride r2 = buildRide(2L, 2L, RideStatus.IN_PROGRESS);
        RideResponseDTO dto1 = buildResponse(1L, 1L, RideStatus.WAITING);
        RideResponseDTO dto2 = buildResponse(2L, 2L, RideStatus.IN_PROGRESS);

        when(repository.findAll()).thenReturn(List.of(r1, r2));
        when(mapper.toResponse(r1)).thenReturn(dto1);
        when(mapper.toResponse(r2)).thenReturn(dto2);

        List<RideResponseDTO> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoRides() {
        when(repository.findAll()).thenReturn(List.of());

        List<RideResponseDTO> result = service.findAll();

        assertTrue(result.isEmpty());
    }

    // --- helpers ---

    private Ride buildRide(Long id, Long userId, RideStatus status) {
        Ride ride = new Ride();
        ride.setId(id);
        ride.setUserId(userId);
        ride.setOrigin("Rua A");
        ride.setDestination("Rua B");
        ride.setStatus(status);
        ride.setCreatedAt(LocalDateTime.now());
        return ride;
    }

    private RideResponseDTO buildResponse(Long id, Long userId, RideStatus status) {
        return new RideResponseDTO(id, userId, "Rua A", "Rua B", status, null, LocalDateTime.now());
    }
}
