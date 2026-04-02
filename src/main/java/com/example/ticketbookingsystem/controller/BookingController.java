package com.example.ticketbookingsystem.controller;

import com.example.ticketbookingsystem.controller.request.BookingRequest;
import com.example.ticketbookingsystem.controller.response.BookingResponse;
import com.example.ticketbookingsystem.controller.response.OrderResponse;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Ticket Booking Flow")
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Place a ticket booking",
            description = "Returns HTTP 202 immediately. Use polling endpoint to check final status."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Order accepted, processing"),
            @ApiResponse(responseCode = "400", description = "Sold out or invalid quantity"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> booking(
            @RequestBody @Valid BookingRequest request,
            @AuthenticationPrincipal User currentUser) { // ← lấy user từ JWT context

        return ResponseEntity.status(HttpStatus.ACCEPTED) // 202
                .body(bookingService.booking(currentUser.getId(), request));
    }

    @Operation(summary = "Poll order status", description = "Call every 2s after placing booking")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/orders/status/{orderId}")
    public ResponseEntity<OrderResponse> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(bookingService.getOrderStatus(orderId));
    }
}
