package br.gov.pge.rides.messaging;

import br.gov.pge.rides.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RideConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.RIDES_QUEUE)
    public void onRideCreated(RideMessage message) {
        log.info("Ride received from the queue: id={}, from '{}' to '{}'",
                message.rideId(), message.origin(), message.destination());
        // Day 4: notify connected drivers via SSE
    }
}
