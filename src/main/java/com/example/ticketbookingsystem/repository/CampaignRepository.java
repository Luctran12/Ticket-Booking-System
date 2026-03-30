package com.example.ticketbookingsystem.repository;

import com.example.ticketbookingsystem.common.CampaignStatus;
import com.example.ticketbookingsystem.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(CampaignStatus status);
}
