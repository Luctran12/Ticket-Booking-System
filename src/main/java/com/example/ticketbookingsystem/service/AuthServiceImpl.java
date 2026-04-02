package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.common.Role;
import com.example.ticketbookingsystem.controller.request.LoginRequest;
import com.example.ticketbookingsystem.controller.request.RegisterRequest;
import com.example.ticketbookingsystem.controller.request.SignInRequest;
import com.example.ticketbookingsystem.controller.response.AuthResponse;
import com.example.ticketbookingsystem.controller.response.TokenResponse;
import com.example.ticketbookingsystem.controller.response.UserResponse;
import com.example.ticketbookingsystem.entity.User;
import com.example.ticketbookingsystem.exception.DuplicateResourceException;
import com.example.ticketbookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

//    @Override
//    public TokenResponse getAccessToken(SignInRequest request) {
//        return null;
//    }
//
//    @Override
//    public TokenResponse getRefreshToken(String request) {
//        return null;
//    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Spring Security tự validate email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // Nếu sai → AuthenticationException tự động throw

        // 2. Load user & generate token
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        // check email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Email already exists: " + request.getEmail()
            );
        }

        // Hash password
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.CUSTOMER) // Default role
                .build();

        user = userRepository.save(user);


        return UserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
