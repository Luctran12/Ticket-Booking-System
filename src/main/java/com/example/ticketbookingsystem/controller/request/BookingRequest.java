package com.example.ticketbookingsystem.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Cannot book more than 10 tickets at once")
    private Integer quantity;
}
