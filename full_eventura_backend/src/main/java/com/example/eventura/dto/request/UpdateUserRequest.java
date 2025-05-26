package com.example.eventura.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String password;
}