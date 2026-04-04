package com.example.ticketbookingsystem.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.booking}")
    private String bookingQueue;

    @Value("${rabbitmq.queue.dead-letter}")
    private String deadLetterQueue;

    @Value("${rabbitmq.routing-key.booking}")
    private String bookingRoutingKey;

    @Value("${rabbitmq.routing-key.dead-letter}")
    private String deadLetterRoutingKey;

    // ── EXCHANGE ──────────────────────────────────────

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange(exchange);
    }

    // ── QUEUES ────────────────────────────────────────

    @Bean
    public Queue bookingQueue() {
        return QueueBuilder.durable(bookingQueue)
                // Khi message fail quá số lần retry → chuyển sang DLQ
                .withArgument("x-dead-letter-exchange", exchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        // DLQ không cần dead-letter tiếp theo
        return QueueBuilder.durable(deadLetterQueue).build();
    }

    // ── BINDINGS ──────────────────────────────────────

    @Bean
    public Binding bookingBinding() {
        return BindingBuilder
                .bind(bookingQueue())
                .to(bookingExchange())
                .with(bookingRoutingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(bookingExchange())
                .with(deadLetterRoutingKey);
    }

    // ── SERIALIZATION ─────────────────────────────────

    // Serialize message thành JSON thay vì Java binary
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
