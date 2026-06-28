package br.gov.pge.rides.messaging;

import br.gov.pge.rides.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RideProducer {

    private static final Logger log = LoggerFactory.getLogger(RideProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public RideProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishRideCreated(RideMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RIDES_EXCHANGE,
                    RabbitMQConfig.RIDES_ROUTING_KEY,
                    message
            );
            log.info("Ride {} published to the queue", message.rideId());
        } catch (Exception ex) {
            log.error("Failed to publish ride {} to the queue", message.rideId(), ex);
        }
    }
}
