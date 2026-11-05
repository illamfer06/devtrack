package com.devtrack.backend.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class DevTrackUserDetails extends org.springframework.security.core.userdetails.User{

    private final Long id;

    public DevTrackUserDetails(
            Long id,
            String email,
            String passwordHash) {
        super(email, passwordHash, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
