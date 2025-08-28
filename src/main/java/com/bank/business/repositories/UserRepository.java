package com.bank.business.repositories;

import com.bank.business.entities.User;
import java.util.List; // Import List

public interface UserRepository
{
    User save(User user);

    User findById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    void deleteById(Long id);

    List<User> findAll(); // New method for Admin functionality
    // Add other necessary methods like update, etc.
}
