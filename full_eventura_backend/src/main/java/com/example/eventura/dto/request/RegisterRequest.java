package com.example.eventura.dto.request;

import com.example.eventura.entity.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String password;
    private User.Role role;
}