package com.sidhant.ticket_management.service;
import java.util.List;
import com.sidhant.ticket_management.dto.request.CreateTicketRequest;
import com.sidhant.ticket_management.entity.BankingClient;
import com.sidhant.ticket_management.entity.Ticket;
import com.sidhant.ticket_management.entity.TicketComment;
import com.sidhant.ticket_management.entity.TicketHistory;
import com.sidhant.ticket_management.entity.TicketStatus;
import com.sidhant.ticket_management.entity.User;
import com.sidhant.ticket_management.repository.BankingClientRepository;
import com.sidhant.ticket_management.repository.TicketCommentRepository;
import com.sidhant.ticket_management.repository.TicketHistoryRepository;
import com.sidhant.ticket_management.repository.TicketRepository;
import com.sidhant.ticket_management.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.sidhant.ticket_management.dto.response.CommentResponse;
import com.sidhant.ticket_management.dto.response.TicketHistoryResponse;
import com.sidhant.ticket_management.dto.response.TicketResponse;
import com.sidhant.ticket_management.exception.ResourceNotFoundException;
@Service
public class TicketService{
    private final TicketHistoryRepository ticketHistoryRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketRepository ticketRepository;
    private final BankingClientRepository bankingClientRepository;
    private final UserRepository userRepository;
    public TicketService(
        TicketRepository ticketRepository,
        BankingClientRepository bankingClientRepository,
        UserRepository userRepository,
        TicketCommentRepository ticketCommentRepository,
        TicketHistoryRepository ticketHistoryRepository) {

    this.ticketRepository = ticketRepository;
    this.bankingClientRepository = bankingClientRepository;
    this.userRepository = userRepository;
    this.ticketCommentRepository = ticketCommentRepository;
    this.ticketHistoryRepository = ticketHistoryRepository;
}

  
    public List<CommentResponse> getComments(
        Long ticketId,
        String requestorEmail) {

        Ticket ticket = ticketRepository
                .findByIdAndRequestorEmail(ticketId, requestorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));

        return ticketCommentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getComment(),
                        comment.getSupportEngineer().getName(),
                        comment.getCreatedAt()
                ))
                .toList();
    }
    public Ticket createTicket(
            CreateTicketRequest request,
            String requestorEmail) {

        User requestor = userRepository
                .findByEmail(requestorEmail)
                .orElseThrow(() ->
                        new RuntimeException("Requestor not found"));

        BankingClient bankingClient = bankingClientRepository
                .findById(request.getBankingClientId())
                .orElseThrow(() ->
                        new RuntimeException("Banking client not found"));

        Ticket ticket = new Ticket();

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setAttachment(request.getAttachment());

        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequestor(requestor);
        ticket.setBankingClient(bankingClient);
        Ticket savedTicket = ticketRepository.save(ticket);

        recordHistory(
                savedTicket,
                requestor,
                "TICKET_CREATED",
                "Ticket created"
        );

        return savedTicket;

        
    }
    public List<TicketResponse> getMyTickets(String requestorEmail) {

    List<Ticket> tickets =
            ticketRepository.findByRequestorEmail(requestorEmail);

    return tickets.stream()
            .map(ticket -> new TicketResponse(
                    ticket.getId(),
                    ticket.getBankingClient().getName(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getAttachment()
            ))
            .toList();
    }
    public TicketResponse getMyTicket(Long ticketId, String requestorEmail) {

    Ticket ticket = ticketRepository
            .findByIdAndRequestorEmail(ticketId, requestorEmail)
            .orElseThrow(() ->
                    new  ResourceNotFoundException("Ticket Not Found!"));

    return new TicketResponse(
            ticket.getId(),
            ticket.getBankingClient().getName(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getAttachment()
    );
}
public List<TicketResponse> getAssignedTickets(
        String supportEngineerEmail) {

    return ticketRepository
            .findBySupportEngineerEmail(supportEngineerEmail)
            .stream()
            .map(ticket -> new TicketResponse(
                    ticket.getId(),
                    ticket.getBankingClient().getName(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getAttachment()
            ))
            .toList();
}
    public TicketResponse getSupportTicket(Long ticketId) {

    Ticket ticket = ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));

    return new TicketResponse(
            ticket.getId(),
            ticket.getBankingClient().getName(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getAttachment()
    );
}
public List<TicketResponse> getOpenTickets() {

    return ticketRepository
            .findByStatus(TicketStatus.OPEN)
            .stream()
            .map(ticket -> new TicketResponse(
                    ticket.getId(),
                    ticket.getBankingClient().getName(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getAttachment()
            ))
            .toList();
}
public TicketResponse assignTicket(
        Long ticketId,
        Long supportEngineerId) {

    Ticket ticket = ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));

    User supportEngineer = userRepository
            .findById(supportEngineerId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Support Engineer not found"));

    if (supportEngineer.getRole() != com.sidhant.ticket_management.entity.Role.SUPPORT_ENGINEER) {
        throw new IllegalArgumentException(
                "User is not a Support Engineer");
    }

    ticket.setSupportEngineer(supportEngineer);

    Ticket savedTicket = ticketRepository.save(ticket);
    recordHistory(
            savedTicket,
            supportEngineer,
            "TICKET_ASSIGNED",
            "Assigned to " + supportEngineer.getEmail()
    );
    return new TicketResponse(
            savedTicket.getId(),
            savedTicket.getBankingClient().getName(),
            savedTicket.getTitle(),
            savedTicket.getDescription(),
            savedTicket.getCategory(),
            savedTicket.getPriority(),
            savedTicket.getStatus(),
            savedTicket.getAttachment()
    );
}
public TicketResponse reassignTicket(
        Long ticketId,
        Long supportEngineerId) {

    Ticket ticket = ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));

    User newSupportEngineer = userRepository
            .findById(supportEngineerId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Support Engineer not found"));

    if (newSupportEngineer.getRole()
            != com.sidhant.ticket_management.entity.Role.SUPPORT_ENGINEER) {

        throw new IllegalArgumentException(
                "User is not a Support Engineer");
    }

    ticket.setSupportEngineer(newSupportEngineer);

    Ticket savedTicket = ticketRepository.save(ticket);
    recordHistory(
            savedTicket,
            newSupportEngineer,
            "TICKET_REASSIGNED",
            "Reassigned to " + newSupportEngineer.getEmail()
    );

    return new TicketResponse(
            savedTicket.getId(),
            savedTicket.getBankingClient().getName(),
            savedTicket.getTitle(),
            savedTicket.getDescription(),
            savedTicket.getCategory(),
            savedTicket.getPriority(),
            savedTicket.getStatus(),
            savedTicket.getAttachment()
    );
}
public CommentResponse addComment(Long ticketId,String supportEngineerEmail,String commentText) {

    Ticket ticket = ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));

    User supportEngineer = userRepository
            .findByEmail(supportEngineerEmail)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Support Engineer not found"));

    if (supportEngineer.getRole()
            != com.sidhant.ticket_management.entity.Role.SUPPORT_ENGINEER) {

        throw new IllegalArgumentException(
                "User is not a Support Engineer");
    }

    TicketComment ticketComment = new TicketComment();

    ticketComment.setComment(commentText);
    ticketComment.setTicket(ticket);
    ticketComment.setSupportEngineer(supportEngineer);
    ticketComment.setCreatedAt(java.time.LocalDateTime.now());
     
    TicketComment savedComment =
            ticketCommentRepository.save(ticketComment);
    recordHistory(
        ticket,
        supportEngineer,
        "COMMENT_ADDED",
        "Comment added by " + supportEngineer.getEmail()
    );

    return new CommentResponse(
            savedComment.getId(),
            savedComment.getComment(),
            savedComment.getSupportEngineer().getName(),
            savedComment.getCreatedAt()
    );
}
public TicketResponse updateTicketStatus(Long ticketId,TicketStatus newStatus) {

    Ticket ticket = ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));
    TicketStatus oldStatus = ticket.getStatus();

    ticket.setStatus(newStatus);

    Ticket savedTicket = ticketRepository.save(ticket);
    recordHistory(
        savedTicket,
        savedTicket.getSupportEngineer(),
        "STATUS_CHANGED",
        oldStatus + " → " + newStatus
    );

    return new TicketResponse(
            savedTicket.getId(),
            savedTicket.getBankingClient().getName(),
            savedTicket.getTitle(),
            savedTicket.getDescription(),
            savedTicket.getCategory(),
            savedTicket.getPriority(),
            savedTicket.getStatus(),
            savedTicket.getAttachment()
    );
}
private void recordHistory(Ticket ticket, User performedBy, String action,String details) {

    TicketHistory history = new TicketHistory();

    history.setTicket(ticket);
    history.setPerformedBy(performedBy);
    history.setAction(action);
    history.setDetails(details);
    history.setCreatedAt(java.time.LocalDateTime.now());

    ticketHistoryRepository.save(history);
}
public List<TicketHistoryResponse> getTicketHistory(Long ticketId) {

    ticketRepository
            .findById(ticketId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Ticket not found"));

    return ticketHistoryRepository
            .findByTicketIdOrderByCreatedAtAsc(ticketId)
            .stream()
            .map(history -> new TicketHistoryResponse(
                    history.getId(),
                    history.getAction(),
                    history.getPerformedBy().getEmail(),
                    history.getDetails(),
                    history.getCreatedAt()
            ))
            .toList();
}
}
