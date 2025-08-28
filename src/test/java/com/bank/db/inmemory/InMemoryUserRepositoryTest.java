package com.bank.db.inmemory;

import com.bank.business.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest
{

    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp()
    {
        userRepository = InMemoryUserRepository.getInstance();
    }

    @Test
    void testSave_NewUser_AssignsId()
    {
        // Arrange
        var user = new User("testuser", "test@example.com");

        // Act
        var savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        // Our implementation uses hashCode as ID, so we just verify it's assigned
        assertTrue(savedUser.getId() > 0); // Should be a positive ID
        assertSame(user, savedUser); // Should return the same instance
    }

    @Test
    void testSave_ExistingUser_Updates()
    {
        // Arrange
        var user = new User("testuser", "test@example.com");
        var savedUser = userRepository.save(user); // ID assigned here
        var assignedId = savedUser.getId();
        var updatedEmail = "newemail@example.com";
        savedUser.setEmail(updatedEmail);

        // Act
        var updatedUser = userRepository.save(savedUser);

        // Assert
        assertSame(savedUser, updatedUser);
        assertEquals(assignedId, updatedUser.getId());
        assertEquals(updatedEmail, updatedUser.getEmail());
    }

    @Test
    void testFindById_UserExists()
    {
        // Arrange
        var user = new User("testuser", "test@example.com");
        var savedUser = userRepository.save(user);
        var id = savedUser.getId();

        // Act
        var foundUser = userRepository.findById(id);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(savedUser, foundUser);
    }

    @Test
    void testFindById_UserNotExists()
    {
        // Arrange
        var nonExistentId = 999L;

        // Act
        var foundUser = userRepository.findById(nonExistentId);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testFindByUsername_UserExists()
    {
        // Arrange
        var username = "uniqueuser" + System.currentTimeMillis(); // Make it unique
        var user = new User(username, "test@example.com");
        userRepository.save(user);

        // Act
        var foundUser = userRepository.findByUsername(username);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(user, foundUser);
    }

    @Test
    void testFindByUsername_UserNotExists()
    {
        // Arrange
        var nonExistentUsername = "nonexistent" + System.currentTimeMillis(); // Make it unique

        // Act
        var foundUser = userRepository.findByUsername(nonExistentUsername);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testFindByEmail_UserExists()
    {
        // Arrange
        var email = "unique" + System.currentTimeMillis() + "@example.com"; // Make it unique
        var user = new User("testuser", email);
        userRepository.save(user);

        // Act
        var foundUser = userRepository.findByEmail(email);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(user, foundUser);
    }

    @Test
    void testFindByEmail_UserNotExists()
    {
        // Arrange
        var nonExistentEmail = "nonexistent" + System.currentTimeMillis() + "@example.com"; // Make it unique

        // Act
        var foundUser = userRepository.findByEmail(nonExistentEmail);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testDeleteById_UserExists()
    {
        // Arrange
        var user = new User("testuser", "test@example.com");
        var savedUser = userRepository.save(user);
        var id = savedUser.getId();

        // Act
        userRepository.deleteById(id);

        // Assert
        assertFalse(userRepository.findById(id) != null);
    }

    @Test
    void testDeleteById_UserNotExists()
    {
        // Arrange
        var nonExistentId = 999L;

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> userRepository.deleteById(nonExistentId));
    }


    @Test
    void testFindAll_WithUsers()
    {
        // Get initial count
        int usersSizeBefore = userRepository.findAll().size();

        // Arrange
        var uniqueSuffix = String.valueOf(System.currentTimeMillis());
        var user1 = new User("user1" + uniqueSuffix, "u1" + uniqueSuffix + "@example.com");
        var user2 = new User("user2" + uniqueSuffix, "u2" + uniqueSuffix + "@example.com", true); // Admin user
        var user3 = new User("user3" + uniqueSuffix, "u3" + uniqueSuffix + "@example.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Act
        var users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        // We should have at least 3 more users than before
        assertTrue(users.size() >= 3 + usersSizeBefore);
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));
        // Verify the admin user is correctly identified
        var foundAdmin = users.stream().filter(User::isAdmin).findFirst().orElse(null);
        assertTrue(foundAdmin != null);
        assertEquals(user2, foundAdmin);
    }

    @Test
    void testFindAll_ReturnsCopy()
    {
        // Arrange
        var uniqueSuffix = String.valueOf(System.currentTimeMillis());
        var user1 = new User("user1" + uniqueSuffix, "u1" + uniqueSuffix + "@example.com");
        userRepository.save(user1);
        var usersFromRepo = userRepository.findAll();
        var expectedSize = usersFromRepo.size();

        // Act - Modify the returned list
        usersFromRepo.clear();

        // Assert - The internal store should not be affected
        var usersFromRepoAgain = userRepository.findAll();
        assertEquals(expectedSize, usersFromRepoAgain.size());
        assertTrue(usersFromRepoAgain.contains(user1));
    }
}
