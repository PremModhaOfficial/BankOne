package com.bank.business.entities.dto;

/**
 * Data Transfer Object for creating a new User.
 * This DTO excludes server-managed fields like id, createdAt, and updatedAt.
 */
public class UserCreationRequest {
    private String username;
    private String email;
    private String password; // Plain text password for request, will be hashed by the server
    private boolean isAdmin;

    // Constructors
    public UserCreationRequest() {
    }

    public UserCreationRequest(String username, String email, String password) {
        this(username, email, password, false);
    }

    public UserCreationRequest(String username, String email, String password, boolean isAdmin) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "UserCreationRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
