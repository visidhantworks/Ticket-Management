package com.sidhant.ticket_management.dto.response;

import com.sidhant.ticket_management.entity.Role;

public class LoginResponse {

    private String token;
    private Role role;

    public LoginResponse(String token, Role role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Role getRole() {
        return role;
    }
}