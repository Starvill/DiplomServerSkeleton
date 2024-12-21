package com.example.user_service;

import lombok.Data;

@Data
public class SigninRequest {
    private String username;
    private String password;
}