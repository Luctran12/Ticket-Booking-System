package com.example.ticketbookingsystem.controller.response;

import com.example.ticketbookingsystem.common.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingResponse {
    private String orderId;
    private OrderStatus orderStatus;
    private String message;
}
