package com.devtrack.backend.dto;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private LocalDateTime createdAt;

    public UserResponse(
            Long id,
            String email,
            LocalDateTime createdAt
    ) {
      this.id = id;
      this.email = email;
      this.createdAt = createdAt;
    }

    public Long getId() { return id;}
    public String getEmail() { return email;}
    public LocalDateTime getCreatedAt() { return createdAt;}
}
