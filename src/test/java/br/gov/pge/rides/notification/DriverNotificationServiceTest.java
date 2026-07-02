package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideMessage;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverNotificationServiceTest {

    @Mock
    private RideRepository repository;

    private DriverNotificationService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.findByStatus(RideStatus.WAITING)).thenReturn(List.of());
        service = new DriverNotificationService(repository);
    }

    @Test
    void subscribe_shouldReturnEmitter() {
        SseEmitter emitter = service.subscribe(1L);

        assertNotNull(emitter);
    }

    @Test
    void subscribe_shouldReplayRidesAlreadyWaiting() {
        Ride waiting = buildWaitingRide(7L);
        when(repository.findByStatus(RideStatus.WAITING)).thenReturn(List.of(waiting));

        assertDoesNotThrow(() -> service.subscribe(1L));
    }

    private Ride buildWaitingRide(Long id) {
        Ride ride = new Ride();
        ride.setId(id);
        ride.setUserId(1L);
        ride.setOrigin("Rua A");
        ride.setDestination("Rua B");
        ride.setStatus(RideStatus.WAITING);
        ride.setCreatedAt(LocalDateTime.now());
        return ride;
    }

    @Test
    void subscribe_shouldReplaceEmitterForSameDriver() {
        SseEmitter first = service.subscribe(1L);
        SseEmitter second = service.subscribe(1L);

        assertNotSame(first, second);
    }

    @Test
    void notifyNewRide_shouldNotThrowWhenNoDriversConnected() {
        RideMessage message = new RideMessage(1L, 1L, "A", "B");

        assertDoesNotThrow(() -> service.notifyNewRide(message));
    }

    @Test
    void notifyNewRide_shouldSendEventToSubscribedDriver() throws Exception {
        service.subscribe(1L);
        RideMessage message = new RideMessage(10L, 5L, "Centro", "Praia");

        assertDoesNotThrow(() -> service.notifyNewRide(message));
    }

    @Test
    void notifyNewRide_shouldBroadcastToMultipleDrivers() {
        service.subscribe(1L);
        service.subscribe(2L);
        RideMessage message = new RideMessage(10L, 5L, "Centro", "Praia");

        assertDoesNotThrow(() -> service.notifyNewRide(message));
    }
}
