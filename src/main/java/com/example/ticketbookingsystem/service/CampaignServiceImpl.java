package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.CampaignStatus;
import com.example.ticketbookingsystem.controller.request.CampaignRequest;
import com.example.ticketbookingsystem.controller.response.CampaignResponse;
import com.example.ticketbookingsystem.entity.Campaign;
import com.example.ticketbookingsystem.exception.ResourceNotFoundException;
import com.example.ticketbookingsystem.repository.CampaignRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignResponse createCampaign(CampaignRequest campaign) {
        Campaign campaignEntity = Campaign.builder()
                .name(campaign.getName())
                .startTime(campaign.getStartTime())
                .endTime(campaign.getEndTime())
                .build();
        campaignEntity = campaignRepository.save(campaignEntity);

        return CampaignResponse.builder()
                .name(campaignEntity.getName())
                .startTime(campaignEntity.getStartTime())
                .endTime(campaignEntity.getEndTime())
                .status(campaignEntity.getStatus().toString())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CampaignResponse> getOngoingCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByStatus(CampaignStatus.ONGOING);

        return campaigns.stream().map(campaign ->
                CampaignResponse.builder()
                        .name(campaign.getName())
                        .startTime(campaign.getStartTime())
                        .endTime(campaign.getEndTime())
                        .status(campaign.getStatus().toString())
                        .build()).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CampaignResponse getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .map(campaign -> CampaignResponse.builder()
                        .name(campaign.getName())
                        .startTime(campaign.getStartTime())
                        .endTime(campaign.getEndTime())
                        .status(campaign.getStatus().toString())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    @Override
    public List<CampaignResponse> getCampaignByName(String name) {
        return campaignRepository.findAllByName(name)
                .stream()
                .map(campaign -> CampaignResponse.builder()
                        .name(campaign.getName())
                        .startTime(campaign.getStartTime())
                        .endTime(campaign.getEndTime())
                        .status(campaign.getStatus().toString())
                        .build())
                .toList();
    }

    //TODO
    //implement Redis first
    @Override
    public void preheatCache(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        // TODO: load tickets → push to Redis
        campaign.setStatus(CampaignStatus.ONGOING);
        campaignRepository.save(campaign);

    }
}
