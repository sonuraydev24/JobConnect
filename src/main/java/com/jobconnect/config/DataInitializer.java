
package com.jobconnect.config;

import com.jobconnect.model.AppUser;
import com.jobconnect.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {

        return args -> {

            if (!users.existsByEmailIgnoreCase(email)) {

                AppUser admin = new AppUser(
                        "JobConnect Admin",
                        email,
                        encoder.encode(password),
                        com.jobconnect.model.Role.ROLE_ADMIN
                );

                users.save(admin);

                System.out.println("JobConnect admin created: " + email);

            } else {
                System.out.println("JobConnect admin already exists.");
            }
        };
    }
}
