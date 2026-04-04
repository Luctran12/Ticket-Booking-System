package com.example.ticketbookingsystem.mq;

import com.example.ticketbookingsystem.common.OrderStatus;

import com.example.ticketbookingsystem.controller.request.BookingMessage;

import com.example.ticketbookingsystem.entity.Order;
import com.example.ticketbookingsystem.entity.Ticket;
import com.example.ticketbookingsystem.exception.ResourceNotFoundException;
import com.example.ticketbookingsystem.repository.OrderRepository;
import com.example.ticketbookingsystem.repository.TicketRepository;
import com.example.ticketbookingsystem.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j(topic = "BOOKING-CONSUMER")
public class BookingConsumer {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final RedisService redisService;

    //private static final int MAX_RETRY = 3;

    @RabbitListener(queues = "${rabbitmq.queue.booking}")
    public void processBooking(BookingMessage message) {
        log.info("Processing booking: orderId={}", message.getOrderId());
        executeBooking(message); // throw exception → RabbitMQ retry
    }

    // ── DLQ Consumer ──────────────────────────────────
    // process  orders failed after all retries
    @RabbitListener(queues = "${rabbitmq.queue.dead-letter}")
    public void handleDeadLetter(BookingMessage message) {
        log.error("Order permanently failed, moving to DLQ: orderId={}",
                message.getOrderId());

        // throw exception if not exist for RabbitMQ retry
        Order order = orderRepository.findById(UUID.fromString(message.getOrderId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found in DB: " + message.getOrderId()));
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);

        // return stocks to redis
        redisService.increaseStock(message.getTicketId(), message.getQuantity());

        // update Redis status for client polling know
        redisService.setOrderStatus(message.getOrderId(), OrderStatus.FAILED);

        log.info("Restored {} stock for ticket {} after DLQ",
                message.getQuantity(), message.getTicketId());
    }

    // ── Private helpers ───────────────────────────────

//    private void processWithRetry(BookingMessage message, int attempt) {
//        try {
//            executeBooking(message);
//        } catch (OptimisticLockingFailureException e) {
//
//            if (attempt < MAX_RETRY) {
//                log.warn("Optimistic lock conflict, retry {}/{} for orderId={}",
//                        attempt + 1, MAX_RETRY, message.getOrderId());
//                processWithRetry(message, attempt + 1);
//            } else {
//                throw e; // hết retry → throw lên để vào DLQ
//            }
//        }
//    }

    @Transactional
    public void executeBooking(BookingMessage message) {
        // 1. Lấy ticket (có Optimistic Lock)
        Ticket ticket = ticketRepository.findById(message.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + message.getTicketId()));

        // 2. Trừ stock DB (Optimistic Lock tầng 2 phát huy ở đây)
        ticket.decreaseStock(message.getQuantity());
        ticketRepository.save(ticket);

        // 3. Cập nhật order → SUCCESS
        orderRepository.findById(UUID.fromString(message.getOrderId()))
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.SUCCESS);
                    orderRepository.save(order);
                });

        // 4. Cập nhật Redis → client polling nhận được SUCCESS
        redisService.setOrderStatus(message.getOrderId(), OrderStatus.SUCCESS);

        log.info("Booking SUCCESS: orderId={}", message.getOrderId());
    }
}
