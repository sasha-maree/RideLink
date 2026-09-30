package com.ridelink.account.dto;

import com.ridelink.account.model.Role;
import java.util.UUID;

public class LoginData {
    private String token;
    private String tokenType = "Bearer";
    private UUID accountId;
    private Role role;

    public LoginData() {}

    public LoginData(String token, UUID accountId, Role role) {
        this.token = token;
        this.accountId = accountId;
        this.role = role;
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

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
