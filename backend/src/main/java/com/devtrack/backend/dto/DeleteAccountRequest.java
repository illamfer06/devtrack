package com.devtrack.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class DeleteAccountRequest {

    @NotBlank(message = "Password is required")
    private String password;

    public DeleteAccountRequest(){}

    public String getPassword() { return password;}
    public void setPassword(String password) {
        this.password = password;
    }
}
