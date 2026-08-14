package com.sidhant.ticket_management.dto.response;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String comment;
    private String supportEngineer;
    private LocalDateTime createdAt;

    public CommentResponse(
            Long id,
            String comment,
            String supportEngineer,
            LocalDateTime createdAt) {

        this.id = id;
        this.comment = comment;
        this.supportEngineer = supportEngineer;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public String getSupportEngineer() {
        return supportEngineer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}