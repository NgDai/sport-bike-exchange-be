package com.bicycle.marketplace.config;

import com.bicycle.marketplace.Repository.IUserRepository;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Log
public class ApplicationInitConfig {

    private PasswordEncoder passwordEncoder;
    @Bean
    ApplicationRunner applicationRunner(IUserRepository userRepository) {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());
                Users user = Users.builder()
                        .username("admin")
                        .role(roles)
                        .password(passwordEncoder.encode("1"))
                        .fullName("Administrator")
                        .build();
                userRepository.save(user);
                log.warning("Default admin user created with username 'admin' and password '1'. Please change the password after first login.");
            }
        };
    }
}
