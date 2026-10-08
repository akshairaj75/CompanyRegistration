package com.backend.companyapp.config;

import com.backend.companyapp.entity.User;
import com.backend.companyapp.enums.UserRole;
import com.backend.companyapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin =new User();
            admin.setUsername("SuperAdmin");
            admin.setEmail("superadmin@companyregistry.com");
            admin.setPassword(passwordEncoder.encode("companyAdmin"));
            admin.setRole(UserRole.SUPER_ADMIN);
            userRepository.save(admin);
            logger.info("Default administrator account created: username='SuperAdmin', password='companyAdmin', email='superadmin@companyregistry.com'");
        } else {
            logger.info("Users table already initialized with {} user(s).", userRepository.count());
        }
    }
}
