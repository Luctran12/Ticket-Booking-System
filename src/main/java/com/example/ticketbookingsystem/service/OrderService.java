package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.OrderRequest;
import com.example.ticketbookingsystem.controller.response.OrderResponse;

public interface OrderService {
    // check JWT (user authentication)
    // run Lua script on redis to substract stick
    // redis.call('get', KEYS[1])
    // create a temporary order id
    // send mess to rabbitmq
    // return for front-end
    public OrderResponse placeOrder(OrderRequest request);
}
