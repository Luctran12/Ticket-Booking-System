package com.example.ticketbookingsystem.controller;

import com.example.ticketbookingsystem.controller.request.CampaignRequest;
import com.example.ticketbookingsystem.controller.response.CampaignResponse;
import com.example.ticketbookingsystem.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Flash Sale Campaign Management")
public class CampaignController {
    private final CampaignService campaignService;

    // Public
    @Operation(summary = "Get all ongoing campaigns", description = "Public endpoint — no auth required")
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getOngoingCampaigns() {
        return ResponseEntity.ok(campaignService.getOngoingCampaigns());
    }

    // Admin only — SecurityConfig đã chặn ở filter level
    @Operation(summary = "Create new campaign", description = "ADMIN only")
    @SecurityRequirement(name = "Bearer Authentication")  // ← hiện nút Authorize trên UI
    @PostMapping("/admin/campaigns")
    public ResponseEntity<CampaignResponse> createCampaign(
            @RequestBody @Valid CampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(request));
    }

    @Operation(summary = "Pre-heat ticket stock into Redis cache", description = "ADMIN only — call before flash sale starts")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/admin/campaigns/{id}/preheat")
    public ResponseEntity<Void> preheatCache(@PathVariable Long id) {
        campaignService.preheatCache(id);
        return ResponseEntity.ok().build();
    }
}
