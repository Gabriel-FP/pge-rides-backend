package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideStatusUpdate;
import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientNotificationServiceTest {

    @Mock
    private RideRepository repository;

    private ClientNotificationService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.findFirstByUserIdAndStatusIn(any(), any())).thenReturn(Optional.empty());
        service = new ClientNotificationService(repository);
    }

    @Test
    void subscribe_shouldReturnEmitter() {
        SseEmitter emitter = service.subscribe(1L);

        assertNotNull(emitter);
    }

    @Test
    void subscribe_shouldReplayActiveRideStatus() {
        Ride active = buildRide(7L, 1L, RideStatus.IN_PROGRESS);
        when(repository.findFirstByUserIdAndStatusIn(eq(1L), any())).thenReturn(Optional.of(active));

        assertDoesNotThrow(() -> service.subscribe(1L));
    }

    @Test
    void subscribe_shouldReplaceEmitterForSameUser() {
        SseEmitter first = service.subscribe(1L);
        SseEmitter second = service.subscribe(1L);

        assertNotSame(first, second);
    }

    @Test
    void notifyStatusChange_shouldNotThrowWhenUserNotConnected() {
        RideStatusUpdate update = new RideStatusUpdate(1L, 99L, RideStatus.IN_PROGRESS, 7L);

        assertDoesNotThrow(() -> service.notifyStatusChange(update));
    }

    @Test
    void notifyStatusChange_shouldSendEventOnlyToTargetUser() {
        service.subscribe(1L);
        service.subscribe(2L);

        RideStatusUpdate update = new RideStatusUpdate(10L, 1L, RideStatus.IN_PROGRESS, 7L);

        assertDoesNotThrow(() -> service.notifyStatusChange(update));
    }

    @Test
    void notifyStatusChange_shouldSendCompletedStatus() {
        service.subscribe(1L);
        RideStatusUpdate update = new RideStatusUpdate(10L, 1L, RideStatus.COMPLETED, 7L);

        assertDoesNotThrow(() -> service.notifyStatusChange(update));
    }

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
}
