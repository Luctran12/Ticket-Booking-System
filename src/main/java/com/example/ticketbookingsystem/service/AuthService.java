package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.RegisterRequest;
import com.example.ticketbookingsystem.entity.User;

public interface AuthService {

    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(String request);

}
