package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.OrderStatus;
import com.example.ticketbookingsystem.controller.request.BookingMessage;
import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.entity.Order;
import com.example.ticketbookingsystem.entity.Ticket;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.exception.ResourceNotFoundException;
import com.example.ticketbookingsystem.exception.SoldOutException;
import com.example.ticketbookingsystem.mq.BookingProducer;
import com.example.ticketbookingsystem.repository.OrderRepository;
import com.example.ticketbookingsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "BOOKING-SERVICE")
public class BookingServiceImpl implements BookingService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final RedisService redisService;
    private final BookingProducer bookingProducer;


    /**
     * Processing direct to database
     * TODO: ensure db commit before push MQ
     * Use TransactionalEventListener  to push MQ after transaction commit
     */
    @Transactional
    @Override
    public BookingResponse booking(Long userId, BookingRequest request) {

        // check ticket exist
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found: " + request.getTicketId()));

        // Redis Lua Script — atomic check & decrement
        long result = redisService.decreaseStock(
                request.getTicketId(), request.getQuantity());

        if (result == -2L) {
            throw new ResourceNotFoundException(
                    "Campaign not started or cache not initialized");
        }
        if (result == -1L) {
            throw new SoldOutException(
                    "Ticket is sold out: " + request.getTicketId());
        }

        // create PENDING order in DB
        Order order = Order.builder()
                .user(User.builder().id(userId).build())
                .ticket(ticket)
                .quantity(request.getQuantity())
                .totalPrice(ticket.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity())))
                .priceAtPurchase(ticket.getPrice())
                .status(OrderStatus.PENDING)
                .build();

        // save db and redis
        orderRepository.save(order);
        redisService.setOrderStatus(order.getId().toString(), OrderStatus.PENDING);

        // push to RabbitMQ → return 202 immediately
        BookingMessage message = BookingMessage.builder()
                .orderId(order.getId().toString())
                .userId(userId)
                .ticketId(request.getTicketId())
                .quantity(request.getQuantity())
                .priceAtPurchase(ticket.getPrice())
                .build();

        bookingProducer.sendBookingMessage(message);

        // HTTP 202 — not waiting DB process
        return BookingResponse.builder()
                .orderId(order.getId().toString())
                .orderStatus(OrderStatus.PENDING)
                .message("Your order is being processed!")
                .build();
    }

//    private BookingResponse processBookingAsync(Long userId, BookingRequest request) {
//        log.info("Starting processBookingAsync");
//
//        String orderId = UUID.randomUUID().toString();
//
//
//        redisService.setOrderStatus(orderId, OrderStatus.PENDING);
//
//
//        BookingMessage message = new BookingMessage(
//                orderId, userId, request.getTicketId(), request.getQuantity()
//        );
//        mqProducer.sendBookingMessage(message);
//
//
//        return BookingResponse.builder()
//                .orderId(orderId)
//                .orderStatus(OrderStatus.PENDING)
//                .message("Your order is queued and being processed!")
//                .build();
//    }

    // Polling: read from Redis
    @Override
    public OrderResponse getOrderStatus(String orderId) {

        // Readfrom Redis (quick)
        OrderStatus cachedStatus = redisService.getOrderStatus(orderId);
        if (cachedStatus != null) {
            return OrderResponse.builder()
                    .orderId(orderId)
                    .status(cachedStatus)
                    .build();
        }

        // Fallback: read from DB is cache missed(TTL expired)
        // check orderID format
        UUID uuid;
        try {
            uuid = UUID.fromString(orderId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Invalid order ID format: " + orderId);
        }

        Order order = orderRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId
                ));

        return OrderResponse.builder()
                .orderId(order.getId().toString())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
