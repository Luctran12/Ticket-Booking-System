package com.example.ticketbookingsystem.controller;

import com.example.ticketbookingsystem.controller.request.CampaignRequest;
import com.example.ticketbookingsystem.controller.response.CampaignResponse;
import com.example.ticketbookingsystem.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignService campaignService;

    // Public — ai cũng xem được
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getOngoingCampaigns() {
        return ResponseEntity.ok(campaignService.getOngoingCampaigns());
    }

    // Admin only — SecurityConfig đã chặn ở filter level
    @PostMapping("/admin/campaigns")
    public ResponseEntity<CampaignResponse> createCampaign(
            @RequestBody @Valid CampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(request));
    }

    @PostMapping("/admin/campaigns/{id}/preheat")
    public ResponseEntity<Void> preheatCache(@PathVariable Long id) {
        campaignService.preheatCache(id);
        return ResponseEntity.ok().build();
    }
}
