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
    public User createUser(String username, String email, String hashedPassword, boolean isAdmin) {
        // Add validation logic here if needed (e.g., prevent duplicate usernames/emails)
        User user = new User(username, email, hashedPassword, isAdmin);
        return userRepository.save(user);
    }

    // Convenience method if creating regular users is common
    public User createUser(String username, String email, String password) {
        return createUser(username, email, password, false); // Defaults to non-admin
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
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}