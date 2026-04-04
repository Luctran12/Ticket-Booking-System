package com.example.ticketbookingsystem.mq;

import com.example.ticketbookingsystem.controller.request.BookingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// mq/BookingProducer.java
@Component
@RequiredArgsConstructor
@Slf4j(topic = "RABBITMQ")
public class BookingProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key.booking}")
    private String bookingRoutingKey;

    public void sendBookingMessage(BookingMessage message) {
        rabbitTemplate.convertAndSend(exchange, bookingRoutingKey, message);
        log.info("Sent booking message to queue: orderId={}, ticketId={}",
                message.getOrderId(), message.getTicketId());
    }
}
