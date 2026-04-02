package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.OrderStatus;
import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.entity.Order;
import com.example.ticketbookingsystem.entity.Ticket;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.exception.ResourceNotFoundException;
import com.example.ticketbookingsystem.exception.SoldOutException;
import com.example.ticketbookingsystem.repository.OrderRepository;
import com.example.ticketbookingsystem.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    //TODO
    private final RedisService redisService;
    //private final RabbitMQProducer mqProducer;


    /**
     * Processing direct to database
     * TODO: Redis check -> MQ -> Consumer
     */
    @Override
    public BookingResponse booking(Long userId, BookingRequest request) {

        //check & subtract stock (Optimistic Lock)
        //TODO: Redis Lua Script
        Long result = redisService.decreaseStock(
                request.getTicketId(), request.getQuantity());

        if (result == null || result.equals(-2L)) {
            throw new ResourceNotFoundException(
                    "Campaign not started or cache not initialized");
        }
        if (result.equals(-1L)) {
            throw new SoldOutException("Ticket is sold out: " + request.getTicketId());
        }

        return processBooking(userId, request);
    }

    private BookingResponse processBooking(Long userId, BookingRequest request) {

        //Check ticket exist
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + request.getTicketId()));

        //create order
        Order order = Order.builder()
                .user(User.builder().id(userId).build())
                .ticket(ticket)
                .quantity(request.getQuantity())
                .totalPrice(
                        ticket.getPrice().multiply(
                                BigDecimal.valueOf(request.getQuantity())
                        )
                )
                .priceAtPurchase(ticket.getPrice())
                .status(OrderStatus.PENDING)
                .build();
        orderRepository.save(order);

        // save status PENDING to Redis for client polling
        redisService.setOrderStatus(order.getId().toString(), OrderStatus.PENDING);
        //reponse
        //TODO: MQ: return 202 instantly
        // mqProducer.sendBookingMessage(order.getId(), userId,
        //     request.getTicketId(), request.getQuantity());

        // Tạm thời: xử lý thẳng DB, update status luôn
        ticket.decreaseStock(request.getQuantity()); // Optimistic Lock tầng 2
        ticketRepository.save(ticket);
        order.setStatus(OrderStatus.SUCCESS);
        orderRepository.save(order);
        redisService.setOrderStatus(order.getId().toString(), OrderStatus.SUCCESS);

        return BookingResponse.builder()
                .orderId(order.getId().toString())
                .orderStatus(OrderStatus.SUCCESS)
                .message("Your order is being processed!")
                .build();
    }

    // Polling: read from Redis
    @Override
    public OrderResponse getOrderStatus(String orderId) {

        // Readfrom Redis (quicl)
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
