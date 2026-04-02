package com.example.ticketbookingsystem.repository;

import com.example.ticketbookingsystem.entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    //List<Ticket> findByCampaignID(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<Ticket> findById(Long id);


    List<Ticket> findByCampaignId(Long campaignId);
}
