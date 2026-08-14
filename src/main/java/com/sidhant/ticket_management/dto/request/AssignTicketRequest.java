package com.sidhant.ticket_management.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {

    @NotNull
    private Long supportEngineerId;

    public Long getSupportEngineerId() {
        return supportEngineerId;
    }

    public void setSupportEngineerId(Long supportEngineerId) {
        this.supportEngineerId = supportEngineerId;
    }
}