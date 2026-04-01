package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.OrderStatus;
import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.entity.Order;
import com.example.ticketbookingsystem.entity.Ticket;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.exception.ResourceNotFoundException;
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
    //private final RedisService redisService;
    //private final RabbitMQProducer mqProducer;


    /**
     * Processing direct to database
     * TODO: Redis check -> MQ -> Consumer
     */
    @Transactional
    @Override
    public BookingResponse booking(Long userId, BookingRequest request) {
        //Check ticket exist
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + request.getTicketId()));

        //check & subtract stock (Optimistic Lock)
        //TODO: Redis Lua Script
        ticket.decreaseStock(request.getQuantity());
        ticketRepository.save(ticket);

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

        //reponse
        //TODO: MQ: return 202 instantly

        return BookingResponse.builder()
                .orderId(order.getId().toString())
                .orderStatus(OrderStatus.PENDING)
                .message("Your order is being processed!")
                .build();
    }

    @Override
    public OrderResponse getOrderStatus(String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
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
