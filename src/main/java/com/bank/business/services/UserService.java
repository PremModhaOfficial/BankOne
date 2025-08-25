package com.bank.business.services;

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;
import java.util.List; // Import List
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Modified to accept isAdmin flag
    public User createUser(String username, String email, boolean isAdmin) {
        // Check for existing user by username
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Check for existing user by email
        existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User(username, email, isAdmin);
        return userRepository.save(user);
    }

    // Convenience method if creating regular users is common
    public User createUser(String username, String email) {
        return createUser(username, email, false); // Defaults to non-admin
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // New method for Admin: Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        // Add validation logic here if needed
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
