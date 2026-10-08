package com.backend.companyapp.dto.auth;

import com.backend.companyapp.entity.User;

public class AuthResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private String fullName;
    private String role;
    private long expiresIn;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String username, String email, String fullName, String role, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public static AuthResponseDto fromEntity(User user, String token) {
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().toString());
        response.setExpiresIn(3600000);
        return response;
    }
}
