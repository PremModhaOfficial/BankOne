package com.prem.business.repositories;

import com.prem.business.entities.User;
import java.util.List; // Import List
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    void deleteById(Long id);
    List<User> findAll(); // New method for Admin functionality
    // Add other necessary methods like update, etc.
}