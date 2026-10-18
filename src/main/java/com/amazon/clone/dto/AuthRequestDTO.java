package com.amazon.clone.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String name;
    private String email;
    private String password;
}