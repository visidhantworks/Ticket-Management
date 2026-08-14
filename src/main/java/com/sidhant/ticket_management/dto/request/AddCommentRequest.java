package com.sidhant.ticket_management.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AddCommentRequest {

    @NotBlank
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}