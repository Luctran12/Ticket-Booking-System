package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.CampaignRequest;
import com.example.ticketbookingsystem.controller.response.CampaignResponse;
import com.example.ticketbookingsystem.entity.Campaign;

import java.util.List;

public interface CampaignService {
    CampaignResponse createCampaign(CampaignRequest campaign);

    List<CampaignResponse> getOngoingCampaigns();

    CampaignResponse getCampaignById(Long id);

    List<CampaignResponse> getCampaignByName(String name);

    void preheatCache(Long campaignId);
}
