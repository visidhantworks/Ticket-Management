package com.sidhant.ticket_management.controller;

import com.sidhant.ticket_management.dto.request.AssignTicketRequest;
import com.sidhant.ticket_management.dto.request.CreateRequestorRequest;
import com.sidhant.ticket_management.dto.request.CreateSupportEngineerRequest;
import com.sidhant.ticket_management.dto.response.SupportPerformanceResponse;
import com.sidhant.ticket_management.dto.response.TicketResponse;
import com.sidhant.ticket_management.entity.User;
import com.sidhant.ticket_management.service.AdminService;
import com.sidhant.ticket_management.service.TicketService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final TicketService ticketService;
    private final AdminService adminService;

    public AdminController(
            TicketService ticketService,
            AdminService adminService) {

        this.ticketService = ticketService;
        this.adminService = adminService;
    }

     

    @PostMapping("/tickets/{ticketId}/assign")
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
    @PostMapping("/users/requestor")
    @ResponseStatus(HttpStatus.CREATED)
    public void createRequestor(
            @Valid @RequestBody CreateRequestorRequest request) {
                adminService.createRequestor(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
    }
    @PostMapping("/users/support")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSupportEngineer(
            @Valid @RequestBody CreateSupportEngineerRequest request) {

        adminService.createSupportEngineer(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
    }
    @GetMapping("/users/support")
    public List<User> getSupportEngineers() {

        return adminService.getSupportEngineers();
    }
    @GetMapping("/support/performance")
    public List<SupportPerformanceResponse> getSupportPerformance() {

        return adminService.getSupportPerformance();
    }
    @GetMapping("/tickets")
    public List<TicketResponse> getAllTickets() {

        return ticketService.getAllTickets();
    }
    @GetMapping("/tickets/{ticketId}/history")
    public ResponseEntity<?> getTicketHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketHistory(ticketId));
    }
        
    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicketById(
            @PathVariable Long ticketId) {

        return ticketService.getSupportTicket(ticketId);
    }
    @GetMapping("/support/{supportEngineerId}/solved-tickets")
    public List<TicketResponse> getSolvedTickets(
            @PathVariable Long supportEngineerId) {

        return adminService.getSolvedTickets(supportEngineerId);
    }
}