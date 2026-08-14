package com.sidhant.ticket_management.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_engineer_id", nullable = false)
    private User supportEngineer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public TicketComment() {
    }

    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public User getSupportEngineer() {
        return supportEngineer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public void setSupportEngineer(User supportEngineer) {
        this.supportEngineer = supportEngineer;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}