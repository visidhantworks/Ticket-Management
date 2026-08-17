package com.sidhant.ticket_management.controller;

import com.sidhant.ticket_management.dto.response.CommentResponse;
import com.sidhant.ticket_management.dto.response.TicketHistoryResponse;
import com.sidhant.ticket_management.dto.response.TicketResponse;
import com.sidhant.ticket_management.service.TicketService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.sidhant.ticket_management.dto.request.AddCommentRequest;
import com.sidhant.ticket_management.dto.request.AssignTicketRequest;
import com.sidhant.ticket_management.dto.request.UpdateTicketStatusRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportController{

    private final TicketService ticketService;

    public SupportController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getAssignedTickets(
            Authentication authentication) {

        String supportEngineerEmail = authentication.getName();

        return ticketService.getAssignedTickets(
                supportEngineerEmail
        );
    }
    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicket(
            @PathVariable Long ticketId) {

        return ticketService.getSupportTicket(ticketId);
    }
    @GetMapping("/tickets/open")
    public List<TicketResponse> getOpenTickets() {

        return ticketService.getOpenTickets();
    }
    @PostMapping("/tickets/{ticketId}/assign")
    @ResponseStatus(HttpStatus.OK)
    public TicketResponse assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {

        return ticketService.assignTicket(
                ticketId,
                request.getSupportEngineerId()
        );
    }
    @PostMapping("/tickets/{ticketId}/reassign")
    public TicketResponse reassignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {

        return ticketService.reassignTicket(
                ticketId,
                request.getSupportEngineerId()
        );
    }
    @PostMapping("/tickets/{ticketId}/comments")
    public CommentResponse addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {

        String supportEngineerEmail = authentication.getName();

        return ticketService.addComment(
                ticketId,
                supportEngineerEmail,
                request.getComment()
        );
    }
    @PostMapping("/tickets/{ticketId}/status")
    public TicketResponse updateTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        return ticketService.updateTicketStatus(
                ticketId,
                request.getStatus()
        );
    }
    @GetMapping("/tickets/{ticketId}/history")
public List<TicketHistoryResponse> getTicketHistory(
        @PathVariable Long ticketId) {

    return ticketService.getTicketHistory(ticketId);
}
    
}
