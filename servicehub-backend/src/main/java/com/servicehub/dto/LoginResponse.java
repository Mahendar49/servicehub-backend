package com.servicehub.dto;

import com.servicehub.entity.Role;

public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final Long id;
    private final String name;
    private final String email;
    private final Role role;

    public LoginResponse(String accessToken, Long id, String name, String email, Role role) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
