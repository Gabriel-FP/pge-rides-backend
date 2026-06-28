package br.gov.pge.rides.notification;

import br.gov.pge.rides.messaging.RideMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class DriverNotificationServiceTest {

    private DriverNotificationService service;

    @BeforeEach
    void setUp() {
        service = new DriverNotificationService();
    }

    @Test
    void subscribe_shouldReturnEmitter() {
        SseEmitter emitter = service.subscribe(1L);

        assertNotNull(emitter);
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
