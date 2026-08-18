package com.sidhant.ticket_management.security;

import com.sidhant.ticket_management.entity.UserSession;
import com.sidhant.ticket_management.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private final UserSessionRepository userSessionRepository;

    public JwtAuthenticationFilter(
            UserSessionRepository userSessionRepository) {

        this.userSessionRepository = userSessionRepository;
    }

    private SecretKey getSigningKey() {
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            String sessionId = claims.get("sessionId", String.class);

            if (email == null ||
                    role == null ||
                    sessionId == null) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Optional<UserSession> session =
                    userSessionRepository.findBySessionId(sessionId);

            if (session.isEmpty()) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            UserSession userSession = session.get();

            if (!userSession.isActive() ||
                    userSession.getExpiresAt()
                            .isBefore(LocalDateTime.now())) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if (!userSession.getUser().getEmail().equals(email)) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}