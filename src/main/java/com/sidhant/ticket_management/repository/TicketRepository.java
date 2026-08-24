package com.sidhant.ticket_management.repository;

import com.sidhant.ticket_management.entity.Ticket;
import com.sidhant.ticket_management.entity.TicketStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByRequestorEmail(String email);

    Optional<Ticket> findByIdAndRequestorEmail(Long id, String email);
    List<Ticket> findBySupportEngineerEmail(String email);
     
    List<Ticket> findByStatus(TicketStatus status);

    long countBySupportEngineerIdAndStatus(
        Long supportEngineerId,
        TicketStatus status);
}