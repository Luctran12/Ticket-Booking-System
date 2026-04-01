package com.example.ticketbookingsystem.controller.response;

import com.example.ticketbookingsystem.common.Role;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class AuthResponse implements Serializable {
    private String token;
    private String email;
    private Role role;
}
