package com.bank.business.entities;

import java.util.Objects;

public class User
{
    private Long id;
    private String username;
    private String email;
    private boolean isAdmin; // New field for Admin role

    public User()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(username, email, isAdmin);
    }

    public User(String username, String email)
    {
        this(username, email, false); // Default to non-admin
    }

    public User(String username, String email, boolean isAdmin)
    {
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;

        this.id = ((long) this.hashCode());
    }

    public User(String username, String email, long id, boolean admin)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isAdmin = admin;
    }

    // Getters and Setters
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public void setAdmin(boolean admin)
    {
        isAdmin = admin;
    }

    @Override
    public String toString()
    {
        return "User{" + "id=" + id + ", username='" + username + '\'' + ", email='" + email + '\'' + ", isAdmin=" + isAdmin + '}';
    }
}
