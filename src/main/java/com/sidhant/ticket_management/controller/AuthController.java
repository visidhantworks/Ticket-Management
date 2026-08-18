package com.sidhant.ticket_management.controller;

import com.sidhant.ticket_management.dto.request.LoginRequest;
import com.sidhant.ticket_management.dto.response.LoginResponse;
import com.sidhant.ticket_management.security.JwtService;
import com.sidhant.ticket_management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService , JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            return ResponseEntity.badRequest()
                    .body("Authorization token is required");
        }

        String token = authorizationHeader.substring(7);

        String sessionId = jwtService.extractSessionId(token);

        authService.logout(sessionId);

        return ResponseEntity.ok("Logged out successfully");
    }
 
}