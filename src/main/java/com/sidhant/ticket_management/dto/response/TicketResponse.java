package com.sidhant.ticket_management.dto.response;

import com.sidhant.ticket_management.entity.Category;
import com.sidhant.ticket_management.entity.Priority;
import com.sidhant.ticket_management.entity.TicketStatus;

public class TicketResponse {

    private Long id;
    private String bankingClient;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private TicketStatus status;
    private String attachment;

    public TicketResponse(
            Long id,
            String bankingClient,
            String title,
            String description,
            Category category,
            Priority priority,
            TicketStatus status,
            String attachment) {

        this.id = id;
        this.bankingClient = bankingClient;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.attachment = attachment;
    }

    public Long getId() {
        return id;
    }

    public String getBankingClient() {
        return bankingClient;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getAttachment() {
        return attachment;
    }
}