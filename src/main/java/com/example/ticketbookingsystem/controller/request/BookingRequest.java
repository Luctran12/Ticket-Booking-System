package com.example.ticketbookingsystem.controller.request;

import lombok.Data;

@Data
public class BookingRequest {
    private Long ticketId;
    private Integer quantity;
}
