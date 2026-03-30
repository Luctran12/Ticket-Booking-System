package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.OrderRequest;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Setter
@Getter
@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request) {

        return null;
    }
}
