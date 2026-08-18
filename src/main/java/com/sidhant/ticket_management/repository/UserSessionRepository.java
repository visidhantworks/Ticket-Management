package com.sidhant.ticket_management.repository;
import com.sidhant.ticket_management.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserSessionRepository  extends JpaRepository< UserSession , Long >{
    Optional<UserSession> findByUser(User user);
    Optional<UserSession> findBySessionId(String sessionId);

    
}
 