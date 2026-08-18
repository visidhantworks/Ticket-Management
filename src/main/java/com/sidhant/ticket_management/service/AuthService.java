package com.sidhant.ticket_management.service;

import com.sidhant.ticket_management.dto.request.LoginRequest;
import com.sidhant.ticket_management.dto.response.LoginResponse;
import com.sidhant.ticket_management.entity.User;
import com.sidhant.ticket_management.entity.UserSession;
import com.sidhant.ticket_management.exception.ActiveSessionException;
import com.sidhant.ticket_management.repository.UserRepository;
import com.sidhant.ticket_management.repository.UserSessionRepository;
import com.sidhant.ticket_management.security.JwtService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            UserSessionRepository userSessionRepository) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<UserSession> existingSession = userSessionRepository.findByUser(user);
        if(existingSession.isPresent()){
                UserSession session = existingSession.get();
                if(session.isActive() && session.getExpiresAt().isAfter(LocalDateTime.now())){
                        throw new ActiveSessionException("User is Currently Lgged in on another Device !!");
                }
        }
        String sessionId = UUID.randomUUID().toString();
        UserSession userSession;
        if(existingSession.isPresent()){
                userSession = existingSession.get();
        }
        else{
                userSession = new UserSession();
        }
        userSession.setUser(user);
        userSession.setSessionId(sessionId);
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        userSession.setActive(true);
        userSessionRepository.save(userSession);

        String token = jwtService.generateToken(user , sessionId);

        return new LoginResponse(token, user.getRole());
    }
    public void logout(String sessionId) {

    Optional<UserSession> existingSession =
            userSessionRepository.findBySessionId(sessionId);

    if (existingSession.isPresent()) {

        UserSession session = existingSession.get();

        session.setActive(false);

        userSessionRepository.save(session);
    }
}
}