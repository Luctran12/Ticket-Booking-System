package com.example.ticketbookingsystem.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserPageResponse extends PageResponseAbstract{
    List<UserResponse> users;
}
