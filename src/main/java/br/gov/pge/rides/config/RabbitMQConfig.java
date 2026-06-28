package br.gov.pge.rides.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RIDES_QUEUE = "rides.queue";
    public static final String RIDES_EXCHANGE = "rides.exchange";
    public static final String RIDES_ROUTING_KEY = "rides.created";

    @Bean
    public Queue ridesQueue() {
        return new Queue(RIDES_QUEUE, true); // durable: survives broker restart
    }

    @Bean
    public DirectExchange ridesExchange() {
        return new DirectExchange(RIDES_EXCHANGE);
    }

    @Bean
    public Binding ridesBinding(Queue ridesQueue, DirectExchange ridesExchange) {
        return BindingBuilder.bind(ridesQueue).to(ridesExchange).with(RIDES_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
