package com.backend.companyapp.serviceImpl;

import com.backend.companyapp.dto.auth.AuthResponseDto;
import com.backend.companyapp.dto.auth.LoginRequestDto;
import com.backend.companyapp.dto.auth.RegisterRequestDto;
import com.backend.companyapp.entity.User;
import com.backend.companyapp.enums.UserRole;
import com.backend.companyapp.repository.UserRepository;
import com.backend.companyapp.security.CustomUserPrincipal;
import com.backend.companyapp.security.JwtService;
import com.backend.companyapp.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override 
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (dto == null || dto.getEmail() == null || dto.getUsername() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, and password are required");
        }

        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.COMPANY_USER);
        user.setUsername(dto.getUsername().trim());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return AuthResponseDto.fromEntity(savedUser, token, jwtService.getExpirationDuration());
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        if (dto == null || dto.getUsernameOrEmail() == null || dto.getPassword() == null
                || dto.getUsernameOrEmail().trim().isEmpty() || dto.getPassword().trim().isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String usernameOrEmail = dto.getUsernameOrEmail().trim();

        // Prevent User Enumeration (CWE-204): Throw identical BadCredentialsException if user is not found
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if account is active/enabled to prevent disabled user authentication bypass
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled. Please contact administrator.");
        }

        // Validate password against hashed password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        return AuthResponseDto.fromEntity(user, token, jwtService.getExpirationDuration());
    }

    @Override
    public AuthResponseDto getCurrentLoggedInUser(CustomUserPrincipal principal) {
        if (principal == null || !principal.isAuthenticated()) {
            throw new BadCredentialsException("User is not logged in");
        }

        Long userId = principal.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return AuthResponseDto.fromEntity(user, null, 0);
    }
}

