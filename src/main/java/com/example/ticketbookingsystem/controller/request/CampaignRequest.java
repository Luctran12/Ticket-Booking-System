package com.example.ticketbookingsystem.controller.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CampaignRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotBlank
    private LocalDateTime startTime;

    @NotBlank
    private LocalDateTime endTime;
}
