package com.example.ticketbookingsystem.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingMessage implements Serializable {
    private String orderId;
    private Long userId;
    private Long ticketId;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
