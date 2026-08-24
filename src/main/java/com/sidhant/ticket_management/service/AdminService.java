package com.sidhant.ticket_management.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sidhant.ticket_management.dto.response.SupportPerformanceResponse;
import com.sidhant.ticket_management.dto.response.TicketResponse;
import com.sidhant.ticket_management.entity.Role;
import com.sidhant.ticket_management.entity.TicketStatus;
import com.sidhant.ticket_management.entity.User;
import com.sidhant.ticket_management.exception.UserAlreadyExistsException;
import com.sidhant.ticket_management.repository.TicketRepository;
import com.sidhant.ticket_management.repository.UserRepository;
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketRepository ticketRepository;
    public AdminService(UserRepository userRepository , PasswordEncoder passwordEncoder, TicketRepository ticketRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ticketRepository = ticketRepository;
    }
    public void createRequestor(String name , String email , String password){
        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsException("User with this Email is already Present!!");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.REQUESTOR);
        userRepository.save(user);
        
    }
    public void createSupportEngineer(String name , String email , String password){
        if(userRepository.findByEmail(email).isPresent()){
            throw new UserAlreadyExistsException("User with this Email is already Present!!");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.SUPPORT_ENGINEER);
        userRepository.save(user);

    }
    public List<SupportPerformanceResponse> getSupportPerformance() {

    List<User> supportEngineers =
            userRepository.findAll()
                    .stream()
                    .filter(user -> user.getRole() == Role.SUPPORT_ENGINEER)
                    .toList();

    return supportEngineers.stream()
            .map(user -> {

                long pendingTickets =
                        ticketRepository.countBySupportEngineerIdAndStatus(
                                user.getId(),
                                TicketStatus.OPEN
                        );

                long attendedTickets =
                        ticketRepository.countBySupportEngineerIdAndStatus(
                                user.getId(),
                                TicketStatus.IN_PROGRESS
                        )
                        + ticketRepository.countBySupportEngineerIdAndStatus(
                                user.getId(),
                                TicketStatus.RESOLVED
                        )
                        + ticketRepository.countBySupportEngineerIdAndStatus(
                                user.getId(),
                                TicketStatus.CLOSED
                        );

                return new SupportPerformanceResponse(
                        user.getId(),
                        user.getName(),
                        pendingTickets,
                        attendedTickets
                );
            })
            .toList();
}
public List<TicketResponse> getSolvedTickets(Long supportEngineerId) {

    return ticketRepository
            .findBySupportEngineerIdAndStatus(
                    supportEngineerId,
                    TicketStatus.RESOLVED
            )
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
public List<User> getSupportEngineers() {

    return userRepository.findAll()
            .stream()
            .filter(user -> user.getRole() == Role.SUPPORT_ENGINEER)
            .toList();
}

}
