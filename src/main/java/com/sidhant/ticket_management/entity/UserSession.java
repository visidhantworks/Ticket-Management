package com.sidhant.ticket_management.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false , unique = true)
    private User user;

    @Column(nullable = false , unique = true)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean active;

    public long getId(){
        return this.Id;
    }
     public void setId(long Id){
        this.Id = Id;
    }
    public User getUser(){
        return this.user;
    }
    public void setUser( User user ){
        this.user = user;
    }
    public String getSessionId(){
        return this.sessionId;
    }
    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }
    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }
        public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    
    
    
}
