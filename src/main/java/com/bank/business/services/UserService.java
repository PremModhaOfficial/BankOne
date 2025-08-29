package com.bank.business.services;

import java.util.List; // Import List

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;

public class UserService
{
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    // Modified to accept isAdmin flag
    public User createUser(String username, String email, String password, boolean isAdmin)
    {
        // Validate password requirements
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one letter and one number");
        }

        // Check for existing user by username
        var existingUser = userRepository.findByUsername(username);
        if (existingUser != null)
        {
            return existingUser;
        }

        // Check for existing user by email
        existingUser = userRepository.findByEmail(email);
        if (existingUser != null)
        {
            return existingUser;
        }

        var user = new User(username, email, password, isAdmin);
        return userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasLetter && hasDigit;
    }

    // Convenience method if creating regular users is common
    public User createUser(String username, String email, String password)
    {
        return createUser(username, email, password, false); // Defaults to non-admin
    }

    public User getUserById(Long id)
    {
        return userRepository.findById(id);
    }

    public User getUserByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }

    public User getUserByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    // New method for Admin: Get all users
    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }

    public User updateUser(User user)
    {
        // Add validation logic here if needed
        return userRepository.save(user);
    }

    public void deleteUser(Long id)
    {
        userRepository.deleteById(id);
    }

    public User authenticateUser(String usernameOrEmail, String password)
    {
        // Find user by username first, then by email
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }

        // Validate password if user exists
        if (user != null && user.validatePassword(password)) {
            return user;
        }

        return null; // Authentication failed
    }
}
