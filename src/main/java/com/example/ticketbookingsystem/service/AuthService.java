package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.LoginRequest;
import com.example.ticketbookingsystem.controller.request.RegisterRequest;
import com.example.ticketbookingsystem.controller.request.SignInRequest;
import com.example.ticketbookingsystem.controller.response.AuthResponse;
import com.example.ticketbookingsystem.controller.response.TokenResponse;
import com.example.ticketbookingsystem.controller.response.UserResponse;
import com.example.ticketbookingsystem.entity.User;

public interface AuthService {

    UserResponse register(RegisterRequest request);

//    TokenResponse getAccessToken(SignInRequest request);
//
//    TokenResponse getRefreshToken(String request);

    AuthResponse login(LoginRequest request);

}
