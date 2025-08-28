package com.bank.db.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List; // Add import
import java.util.Optional;

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;

// Placeholder for a future database implementation.
// This class won't compile until you add actual database logic (e.g., JDBC, JPA).
public class DatabaseUserRepository implements UserRepository
{

    private static Connection instance;
    static private final String URL = "jdbc:sqlite:/home/prem-modha/projects/Motadata/BankOne/Bank/bankData.sqlite";

    public static Connection getInstance()
    {
        if (instance == null)
        {
            try
            {
                instance = DriverManager.getConnection(URL);

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public User save(User user)
    {
        instance = getInstance();
        // Placeholder implementation
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public Optional<User> findById(Long id)
    {
        // Implement database find by ID logic here.
        // For JDBC: Use PreparedStatement to SELECT.
        // For JPA: Use entityManager.find(User.class, id).
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public Optional<User> findByUsername(String username)
    {
        // Implement database find by username logic here.
        // For JDBC: Use PreparedStatement to SELECT.
        // For JPA: Use JPQL query or Criteria API.
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public Optional<User> findByEmail(String email)
    {
        // Implement database find by email logic here.
        // For JDBC: Use PreparedStatement to SELECT.
        // For JPA: Use JPQL query or Criteria API.
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public void deleteById(Long id)
    {
        // Implement database delete by ID logic here.
        // For JDBC: Use PreparedStatement to DELETE.
        // For JPA: Use entityManager.remove() or a JPQL DELETE query.
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }

    @Override
    public List<User> findAll()
    {
        // Implement database find all logic here.
        // For JDBC: Use PreparedStatement to SELECT * FROM users.
        // For JPA: Use entityManager.createQuery("SELECT u FROM User u",
        // User.class).getResultList();
        throw new UnsupportedOperationException("Database implementation not yet provided.");
    }
}
