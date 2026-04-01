package com.example.ticketbookingsystem.controller.request;

import lombok.Data;
import lombok.Getter;


@Data
public class LoginRequest {
    private String email;
    private String password;
}
