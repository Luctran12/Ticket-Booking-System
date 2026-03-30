package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;

public interface BookingService {

    BookingResponse booking(Long userId, BookingRequest request);

    OrderResponse getOrderStatus(String orderId);


}
