package com.example.ticketbookingsystem.service;

import com.example.ticketbookingsystem.controller.request.RegisterRequest;
import com.example.ticketbookingsystem.controller.request.UserUpdateRequest;
import com.example.ticketbookingsystem.controller.response.UserPageResponse;
import com.example.ticketbookingsystem.controller.response.UserResponse;
import com.example.ticketbookingsystem.entity.User;

public interface UserService {

    void updateUser(UserUpdateRequest req);

    UserResponse findById(long id);

    UserPageResponse findAll(String keyword, String sort, int page, int size);
}
