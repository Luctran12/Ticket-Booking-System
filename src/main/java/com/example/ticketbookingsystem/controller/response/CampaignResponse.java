package com.example.ticketbookingsystem.controller.response;



import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CampaignResponse {
    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;
}
