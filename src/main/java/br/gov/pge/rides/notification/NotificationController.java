package br.gov.pge.rides.notification;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class NotificationController {

    private final DriverNotificationService driverNotificationService;
    private final ClientNotificationService clientNotificationService;

    public NotificationController(DriverNotificationService driverNotificationService,
                                  ClientNotificationService clientNotificationService) {
        this.driverNotificationService = driverNotificationService;
        this.clientNotificationService = clientNotificationService;
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter driverStream(@RequestParam Long driverId) {
        return driverNotificationService.subscribe(driverId);
    }

    @GetMapping(value = "/notifications/client-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter clientStream(@RequestParam Long userId) {
        return clientNotificationService.subscribe(userId);
    }
}
