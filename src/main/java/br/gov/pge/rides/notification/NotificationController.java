package br.gov.pge.rides.notification;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationController {

    private final DriverNotificationService notificationService;

    public NotificationController(DriverNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // A driver opens this connection and keeps it open to receive ride events in real time.
    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam Long driverId) {
        return notificationService.subscribe(driverId);
    }
}
