package com.sidhant.ticket_management.controller;
import java.util.List;
import com.sidhant.ticket_management.dto.request.CreateTicketRequest;
import com.sidhant.ticket_management.dto.response.CommentResponse;
import com.sidhant.ticket_management.dto.response.TicketHistoryResponse;
import com.sidhant.ticket_management.dto.response.TicketResponse;
import com.sidhant.ticket_management.entity.Ticket;
import com.sidhant.ticket_management.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    @GetMapping("/my-tickets")
    public List<TicketResponse> getMyTickets(Authentication authentication) {

        String requestorEmail = authentication.getName();

        return ticketService.getMyTickets(requestorEmail);
    }
    @GetMapping("/{ticketId}")
    public TicketResponse getMyTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String requestorEmail = authentication.getName();

        return ticketService.getMyTicket(
                ticketId,
                requestorEmail
        );
    }
    @GetMapping("/{ticketId}/comments")
    public List<CommentResponse> getComments(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String requestorEmail = authentication.getName();

        return ticketService.getComments(
                ticketId,
                requestorEmail
        );
    }
    @GetMapping("/{ticketId}/history")
    public List<TicketHistoryResponse> getHistory(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String requestorEmail = authentication.getName();

        // Verify that this ticket belongs to the logged-in requestor
        ticketService.getMyTicket(ticketId, requestorEmail);

        return ticketService.getTicketHistory(ticketId);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ticket createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        String requestorEmail = authentication.getName();

        return ticketService.createTicket(
                request,
                requestorEmail
        );
    }
}