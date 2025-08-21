package com.prem.infrastructure.persistence.inmemory;

import com.prem.business.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = InMemoryUserRepository.getInstance();
    }

    @Test
    void testSave_NewUser_AssignsId() {
        // Arrange
        User user = new User("testuser", "test@example.com", "hashedPass");

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals(1L, savedUser.getId().longValue()); // First ID should be 1
        assertSame(user, savedUser); // Should return the same instance
    }

    @Test
    void testSave_ExistingUser_Updates() {
        // Arrange
        User user = new User("testuser", "test@example.com", "hashedPass");
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
    void testFindById_UserExists() {
        // Arrange
        User user = new User("testuser", "test@example.com", "hashedPass");
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId();

        // Act
        Optional<User> foundUser = userRepository.findById(id);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser, foundUser.get());
    }

    @Test
    void testFindById_UserNotExists() {
        // Arrange
        Long nonExistentId = 999L;

        // Act
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByUsername_UserExists() {
        // Arrange
        String username = "uniqueuser";
        User user = new User(username, "test@example.com", "hashedPass");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByUsername(username);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    void testFindByUsername_UserNotExists() {
        // Arrange
        String nonExistentUsername = "nonexistent";

        // Act
        Optional<User> foundUser = userRepository.findByUsername(nonExistentUsername);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByEmail_UserExists() {
        // Arrange
        String email = "unique@example.com";
        User user = new User("testuser", email, "hashedPass");
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(user, foundUser.get());
    }

    @Test
    void testFindByEmail_UserNotExists() {
        // Arrange
        String nonExistentEmail = "nonexistent@example.com";

        // Act
        Optional<User> foundUser = userRepository.findByEmail(nonExistentEmail);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testDeleteById_UserExists() {
        // Arrange
        User user = new User("testuser", "test@example.com", "hashedPass");
        User savedUser = userRepository.save(user);
        Long id = savedUser.getId();

        // Act
        userRepository.deleteById(id);

        // Assert
        assertFalse(userRepository.findById(id).isPresent());
    }

    @Test
    void testDeleteById_UserNotExists() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> userRepository.deleteById(nonExistentId));
    }

    @Test
    void testFindAll_EmptyRepository() {
        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testFindAll_WithUsers() {
        // Arrange
        User user1 = new User("user1", "u1@example.com", "pass1");
        User user2 = new User("user2", "u2@example.com", "pass2", true); // Admin user
        User user3 = new User("user3", "u3@example.com", "pass3");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertEquals(3, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));
        // Verify the admin user is correctly identified
        Optional<User> foundAdmin = users.stream().filter(User::isAdmin).findFirst();
        assertTrue(foundAdmin.isPresent());
        assertEquals(user2, foundAdmin.get());
    }

    @Test
    void testFindAll_ReturnsCopy() {
        // Arrange
        User user1 = new User("user1", "u1@example.com", "pass1");
        userRepository.save(user1);
        List<User> usersFromRepo = userRepository.findAll();

        // Act - Modify the returned list
        usersFromRepo.clear();

        // Assert - The internal store should not be affected
        List<User> usersFromRepoAgain = userRepository.findAll();
        assertEquals(1, usersFromRepoAgain.size());
        assertEquals(user1, usersFromRepoAgain.get(0));
    }
}
