package com.example.ticketbookingsystem.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BookingRequest {
    private Long ticketId;
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Cannot book more than 10 tickets at once")
    private Integer quantity;
}
