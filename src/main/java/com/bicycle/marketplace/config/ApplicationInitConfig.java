package com.bicycle.marketplace.config;

import com.bicycle.marketplace.entities.Wallet;
import com.bicycle.marketplace.repository.IUserRepository;
import com.bicycle.marketplace.entities.Users;
import com.bicycle.marketplace.enums.Role;
import com.bicycle.marketplace.repository.IWalletRepository;
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
    ApplicationRunner applicationRunner(IUserRepository userRepository, IWalletRepository walletRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                Users user = Users.builder()
                        .username("admin")
                        .role(Role.ADMIN.name())
                        .password("1") // In a real application, ensure to hash passwords
                        // .passwordHash(passwordEncoder.encode("1"))
                        .fullName("Administrator")
                        .build();
                userRepository.save(user);
                log.warning(
                        "Default admin user created with username 'admin' and password '1'. Please change the password after first login.");
            }

            if (walletRepository.findByType("System").isEmpty()) {
                Wallet wallet = Wallet.builder()
                        .username("System")
                        .balance(0.0)
                        .type("System")
                        .build();
                walletRepository.save(wallet);
                log.warning(
                        "Default system wallet is created please do not change anything.");
            }
        };
    }
}
