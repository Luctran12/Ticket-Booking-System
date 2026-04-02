package com.example.ticketbookingsystem.controller;

import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> booking(
            @RequestBody @Valid BookingRequest request,
            @AuthenticationPrincipal User currentUser) { // ← lấy user từ JWT context

        return ResponseEntity.status(HttpStatus.ACCEPTED) // 202
                .body(bookingService.booking(currentUser.getId(), request));
    }

    @GetMapping("/orders/status/{orderId}")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(bookingService.getOrderStatus(orderId));
    }
}
