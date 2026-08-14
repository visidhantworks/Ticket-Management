package com.sidhant.ticket_management.config;

import com.sidhant.ticket_management.entity.Role;
import com.sidhant.ticket_management.entity.User;
import com.sidhant.ticket_management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            System.out.println("========DATA INITIALIZER RUNNING=======");

            if (userRepository.findByEmail("requestor@test.com").isEmpty()) {

                User requestor = new User();
                requestor.setName("Test Requestor");
                requestor.setEmail("requestor@test.com");
                requestor.setPassword(
                        passwordEncoder.encode("Requestor@123")
                );
                requestor.setRole(Role.REQUESTOR);

                userRepository.save(requestor);
            }

            if (userRepository.findByEmail("support@test.com").isEmpty()) {

                User supportEngineer = new User();
                supportEngineer.setName("Test Support Engineer");
                supportEngineer.setEmail("support@test.com");
                supportEngineer.setPassword(
                        passwordEncoder.encode("Support@123")
                );
                supportEngineer.setRole(Role.SUPPORT_ENGINEER);

                userRepository.save(supportEngineer);
            }
            if (userRepository.findByEmail("support2@test.com").isEmpty()) {
            System.out.println("=======CREATING SUPPORT ENGINEER2=====");

            User supportEngineer2 = new User();
            supportEngineer2.setName("Second Support Engineer");
            supportEngineer2.setEmail("support2@test.com");
            supportEngineer2.setPassword(
                    passwordEncoder.encode("Support2@123")
            );
            supportEngineer2.setRole(Role.SUPPORT_ENGINEER);

            userRepository.save(supportEngineer2);
        }
        };
    }
}