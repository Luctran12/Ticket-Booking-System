package com.example.ticketbookingsystem.controller.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class CampaignRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotBlank
    private LocalDateTime startTime;

    @NotBlank
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
}
