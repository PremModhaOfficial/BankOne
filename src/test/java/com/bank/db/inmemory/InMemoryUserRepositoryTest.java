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
        User user = new User("testuser", "test@example.com");

        // Act
        User savedUser = userRepository.save(user);

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
        User user = new User("testuser", "test@example.com");
        User savedUser = userRepository.save(user); // ID assigned here
        Long assignedId = savedUser.getId();
        String updatedEmail = "newemail@example.com";
        savedUser.setEmail(updatedEmail);

        // Act
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertSame(savedUser, updatedUser);
        assertEquals(assignedId, updatedUser.getId());
        assertEquals(updatedEmail, updatedUser.getEmail());
    }

    @Test
    void testFindById_UserExists()
    {
        // Arrange
        User user = new User("testuser", "test@example.com");
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId();

        // Act
        User foundUser = userRepository.findById(id);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(savedUser, foundUser);
    }

    @Test
    void testFindById_UserNotExists()
    {
        // Arrange
        Long nonExistentId = 999L;

        // Act
        User foundUser = userRepository.findById(nonExistentId);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testFindByUsername_UserExists()
    {
        // Arrange
        String username = "uniqueuser" + System.currentTimeMillis(); // Make it unique
        User user = new User(username, "test@example.com");
        userRepository.save(user);

        // Act
        User foundUser = userRepository.findByUsername(username);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(user, foundUser);
    }

    @Test
    void testFindByUsername_UserNotExists()
    {
        // Arrange
        String nonExistentUsername = "nonexistent" + System.currentTimeMillis(); // Make it unique

        // Act
        User foundUser = userRepository.findByUsername(nonExistentUsername);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testFindByEmail_UserExists()
    {
        // Arrange
        String email = "unique" + System.currentTimeMillis() + "@example.com"; // Make it unique
        User user = new User("testuser", email);
        userRepository.save(user);

        // Act
        User foundUser = userRepository.findByEmail(email);

        // Assert
        assertTrue(foundUser != null);
        assertEquals(user, foundUser);
    }

    @Test
    void testFindByEmail_UserNotExists()
    {
        // Arrange
        String nonExistentEmail = "nonexistent" + System.currentTimeMillis() + "@example.com"; // Make it unique

        // Act
        User foundUser = userRepository.findByEmail(nonExistentEmail);

        // Assert
        assertFalse(foundUser != null);
    }

    @Test
    void testDeleteById_UserExists()
    {
        // Arrange
        User user = new User("testuser", "test@example.com");
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId();

        // Act
        userRepository.deleteById(id);

        // Assert
        assertFalse(userRepository.findById(id) != null);
    }

    @Test
    void testDeleteById_UserNotExists()
    {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> userRepository.deleteById(nonExistentId));
    }


    @Test
    void testFindAll_WithUsers()
    {
        // Get initial count
        int usersSizeBefore = userRepository.findAll().size();

        // Arrange
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        User user1 = new User("user1" + uniqueSuffix, "u1" + uniqueSuffix + "@example.com");
        User user2 = new User("user2" + uniqueSuffix, "u2" + uniqueSuffix + "@example.com", true); // Admin user
        User user3 = new User("user3" + uniqueSuffix, "u3" + uniqueSuffix + "@example.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        // We should have at least 3 more users than before
        assertTrue(users.size() >= 3 + usersSizeBefore);
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));
        // Verify the admin user is correctly identified
        User foundAdmin = users.stream().filter(User::isAdmin).findFirst().orElse(null);
        assertTrue(foundAdmin != null);
        assertEquals(user2, foundAdmin);
    }

    @Test
    void testFindAll_ReturnsCopy()
    {
        // Arrange
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        User user1 = new User("user1" + uniqueSuffix, "u1" + uniqueSuffix + "@example.com");
        userRepository.save(user1);
        List<User> usersFromRepo = userRepository.findAll();
        int expectedSize = usersFromRepo.size();

        // Act - Modify the returned list
        usersFromRepo.clear();

        // Assert - The internal store should not be affected
        List<User> usersFromRepoAgain = userRepository.findAll();
        assertEquals(expectedSize, usersFromRepoAgain.size());
        assertTrue(usersFromRepoAgain.contains(user1));
    }
}
