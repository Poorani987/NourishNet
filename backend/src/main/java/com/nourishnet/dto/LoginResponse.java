package com.nourishnet.dto;

import com.nourishnet.model.UserRole;

public class LoginResponse {
    private String token;
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private String organization;
    private String phone;
    private String address;

    public LoginResponse() {}

    public LoginResponse(String token, String id, String name, String email, 
                         UserRole role, String organization, String phone, String address) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.organization = organization;
        this.phone = phone;
        this.address = address;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
