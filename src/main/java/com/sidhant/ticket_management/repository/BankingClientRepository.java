package com.sidhant.ticket_management.repository;

import com.sidhant.ticket_management.entity.BankingClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankingClientRepository extends JpaRepository<BankingClient, Long> {
}