package com.example.ticketbookingsystem.controller.response;

import com.example.ticketbookingsystem.common.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
}
