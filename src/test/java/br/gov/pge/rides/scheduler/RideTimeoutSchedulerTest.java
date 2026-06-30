package br.gov.pge.rides.scheduler;

import br.gov.pge.rides.model.Ride;
import br.gov.pge.rides.model.enums.RideStatus;
import br.gov.pge.rides.notification.ClientNotificationService;
import br.gov.pge.rides.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideTimeoutSchedulerTest {

    @Mock private RideRepository repository;
    @Mock private ClientNotificationService clientNotificationService;

    private RideTimeoutScheduler scheduler;

    @Test
    void cancelExpiredRides_shouldCancelWaitingRidesPastTimeout() {
        scheduler = new RideTimeoutScheduler(repository, clientNotificationService, 60);

        Ride expired1 = buildWaitingRide(1L);
        Ride expired2 = buildWaitingRide(2L);
        when(repository.findByStatusAndCreatedAtBefore(eq(RideStatus.WAITING), any(LocalDateTime.class)))
                .thenReturn(List.of(expired1, expired2));

        scheduler.cancelExpiredRides();

        assertEquals(RideStatus.CANCELLED, expired1.getStatus());
        assertEquals(RideStatus.CANCELLED, expired2.getStatus());
        verify(repository, times(2)).save(any(Ride.class));
    }

    @Test
    void cancelExpiredRides_shouldNotifyClientForEachCancelledRide() {
        scheduler = new RideTimeoutScheduler(repository, clientNotificationService, 60);

        Ride expired1 = buildWaitingRide(1L);
        Ride expired2 = buildWaitingRide(2L);
        when(repository.findByStatusAndCreatedAtBefore(eq(RideStatus.WAITING), any(LocalDateTime.class)))
                .thenReturn(List.of(expired1, expired2));

        scheduler.cancelExpiredRides();

        verify(clientNotificationService, times(2)).notifyStatusChange(any());
    }

    @Test
    void cancelExpiredRides_shouldDoNothingWhenNoExpiredRides() {
        scheduler = new RideTimeoutScheduler(repository, clientNotificationService, 60);

        when(repository.findByStatusAndCreatedAtBefore(eq(RideStatus.WAITING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.cancelExpiredRides();

        verify(repository, never()).save(any());
        verify(clientNotificationService, never()).notifyStatusChange(any());
    }

    @Test
    void cancelExpiredRides_shouldUseConfiguredTimeoutValue() {
        scheduler = new RideTimeoutScheduler(repository, clientNotificationService, 120);

        when(repository.findByStatusAndCreatedAtBefore(eq(RideStatus.WAITING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now().minusSeconds(120);
        scheduler.cancelExpiredRides();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findByStatusAndCreatedAtBefore(eq(RideStatus.WAITING), captor.capture());

        LocalDateTime threshold = captor.getValue();
        assertEquals(before.getMinute(), threshold.getMinute());
    }

    private Ride buildWaitingRide(Long id) {
        Ride ride = new Ride();
        ride.setId(id);
        ride.setUserId(1L);
        ride.setOrigin("Rua A");
        ride.setDestination("Rua B");
        ride.setStatus(RideStatus.WAITING);
        ride.setCreatedAt(LocalDateTime.now().minusSeconds(120));
        return ride;
    }
}
