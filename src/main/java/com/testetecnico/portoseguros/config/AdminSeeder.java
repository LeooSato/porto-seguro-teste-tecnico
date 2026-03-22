package com.testetecnico.portoseguros.config;

import com.testetecnico.portoseguros.dto.CreateUserDto;
import com.testetecnico.portoseguros.service.UserService;
import java.time.LocalDate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSeeder {

    @Bean
    public ApplicationRunner adminUserInitializer(UserService userService) {
        return args -> {
            // Minimal default admin; adjust credentials as needed or externalize via properties
            CreateUserDto admin = new CreateUserDto(
                    "Admin",
                    "User",
                    LocalDate.of(1990, 1, 1),
                    "admin@lms.com",
                    "+55 11 00000-0000",
                    "123"
            );

            try {
                userService.createAdmin(admin);
            } catch (Exception ignored) {
                // If already exists or validation fails, ignore seeding attempt
            }
        };
    }
}

