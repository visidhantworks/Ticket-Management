package com.sidhant.ticket_management.dto.response;

import java.time.LocalDateTime;

public class TicketHistoryResponse {

    private Long id;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime createdAt;

    public TicketHistoryResponse(
            Long id,
            String action,
            String performedBy,
            String details,
            LocalDateTime createdAt) {

        this.id = id;
        this.action = action;
        this.performedBy = performedBy;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}   