package com.bank.business.entities;

import java.util.Objects;

import com.bank.business.validators.PasswordHelper;

public class User
{
    private Long id;
    private String username;
    private String email;
    private boolean isAdmin;
    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public User()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        var user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(username, email, isAdmin);
    }

    public boolean validatePassword(String triedPassword)
    {
        return PasswordHelper.matchPassword(this.password, triedPassword);
    }

    public User(String username, String email, String password)
    {
        this(username, email, password, false); // Default to non-admin
    }

    public User(String username, String email, String password, boolean isAdmin)
    {
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.password = PasswordHelper.generatePasswordHash(password);
        this.id = Math.abs((long) this.hashCode());
    }

    public User(String username, String email, String password, long id, boolean isAdmin)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.password = PasswordHelper.generatePasswordHash(password);
    }

    public User(String username, String email, String password, long id, boolean isAdmin, boolean alreadyHashed)
    {
        // password is already hashed
        this.username = username;
        this.email = email;
        this.password = alreadyHashed ? password : PasswordHelper.generatePasswordHash(password);
        this.id = id;
        this.isAdmin = isAdmin;
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
