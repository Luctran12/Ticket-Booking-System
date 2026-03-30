package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.entity.Campaign;

import java.util.List;

public interface CampaignService {
    Campaign createCampaign(Campaign campaign);

    List<Campaign> getOngoingCampaigns();

    Campaign getCampaignById(Long id);

    List<Campaign> getCampaignByName(Long id);

    void preheatCache(Long campaignId);
}
