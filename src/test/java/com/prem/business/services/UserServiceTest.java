package com.prem.business.services;

import com.prem.business.entities.User;
import com.prem.business.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    void testCreateUser_DefaultNonAdmin() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String hashedPassword = "hashedPass123";
        User savedUser = new User(username, email, hashedPassword, false); // Explicitly non-admin
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        User result = userService.createUser(username, email, hashedPassword);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword.hashCode(), result.getHashedPassword());
        assertFalse(result.isAdmin()); // Verify default is non-admin
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_WithAdminFlag() {
        // Arrange
        String username = "adminuser";
        String email = "admin@example.com";
        String hashedPassword = "hashedAdminPass123";
        boolean isAdmin = true;
        User savedUser = new User(username, email, hashedPassword, isAdmin);
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        User result = userService.createUser(username, email, hashedPassword, isAdmin);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword.hashCode(), result.getHashedPassword());
        assertTrue(result.isAdmin()); // Verify admin flag is set
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById_UserExists() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User("testuser", "test@example.com", "hashedPass123");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserByUsername_UserExists() {
        // Arrange
        String username = "testuser";
        User mockUser = new User(username, "test@example.com", "hashedPass123");
        mockUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.getUserByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetUserByEmail_UserExists() {
        // Arrange
        String email = "test@example.com";
        User mockUser = new User("testuser", email, "hashedPass123");
        mockUser.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.getUserByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        User user1 = new User("user1", "u1@example.com", "pass1");
        user1.setId(1L);
        User adminUser = new User("admin", "admin@example.com", "adminPass", true);
        adminUser.setId(2L);
        List<User> mockUsers = Arrays.asList(user1, adminUser);

        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(mockUsers, result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser() {
        // Arrange
        User userToUpdate = new User("olduser", "old@example.com", "oldHashedPass");
        userToUpdate.setId(1L);
        userToUpdate.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updatedUser = userService.updateUser(userToUpdate);

        // Assert
        assertSame(userToUpdate, updatedUser); // Should return the same object instance
        assertEquals(userToUpdate.getId(), updatedUser.getId());
        // Verify updatedAt was updated (it should be more recent than createdAt)
        assertTrue(updatedUser.getUpdatedAt().compareTo(updatedUser.getCreatedAt()) >= 0);
        verify(userRepository, times(1)).save(userToUpdate);
    }

    @Test
    void testDeleteUser() {
        // Arrange
        Long userId = 1L;

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
}