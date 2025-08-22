package com.bank.infrastructure.persistence.inmemory;

import com.bank.business.entities.User;
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
        User user = new User("testuser", "test@example.com");
        var a =userRepository.findAll().stream().max((userA, userB) -> userA.getId().compareTo(userB.getId()));
        long id = a.get().getId();

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals(id+1L, savedUser.getId().longValue()); // First ID should be 1
        assertSame(user, savedUser); // Should return the same instance
    }

    @Test
    void testSave_ExistingUser_Updates() {
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
    void testFindById_UserExists() {
        // Arrange
        User user = new User("testuser", "test@example.com");
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
        User user = new User(username, "test@example.com");
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
        User user = new User("testuser", email);
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
        User user = new User("testuser", "test@example.com");
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
    void testFindAll_WithUsers() {
        int usersSizeBefore = userRepository.findAll().size();
        // Arrange
        User user1 = new User("user1", "u1@example.com");
        User user2 = new User("user2", "u2@example.com", true); // Admin user
        User user3 = new User("user3", "u3@example.com");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertNotNull(users);
        assertEquals(3 + usersSizeBefore, users.size());
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
        User user1 = new User("user1", "u1@example.com");
        userRepository.save(user1);
        List<User> usersFromRepo = userRepository.findAll();
        int expectedSize = usersFromRepo.size();

        // Act - Modify the returned list
        usersFromRepo.clear();

        // Assert - The internal store should not be affected
        List<User> usersFromRepoAgain = userRepository.findAll();
        assertEquals(expectedSize, usersFromRepoAgain.size());
        assertEquals(true, usersFromRepoAgain.contains(user1));
    }
}
