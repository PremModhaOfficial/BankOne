package com.bank.business.services;

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest
{

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    void testCreateUser_DefaultNonAdmin()
    {
        // Arrange
        var username = "testuser";
        var email = "test@example.com";
        var password = "password123";
        var savedUser = new User(username, email, password, false); // Explicitly non-admin
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        var result = userService.createUser(username, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertFalse(result.isAdmin()); // Verify default is non-admin
        assertNotNull(result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_WithAdminFlag()
    {
        // Arrange
        var username = "adminuser";
        var email = "admin@example.com";
        var password = "adminpass123";
        var isAdmin = true;
        var savedUser = new User(username, email, password, isAdmin);
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        var result = userService.createUser(username, email, password, isAdmin);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertTrue(result.isAdmin()); // Verify admin flag is set
        assertNotNull(result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById_UserExists()
    {
        // Arrange
        var userId = 1L;
        var mockUser = new User("testuser", "test@example.com", "password123", userId, false, true);

        when(userRepository.findById(userId)).thenReturn(mockUser);

        // Act
        var result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_UserNotFound()
    {
        // Arrange
        var userId = 999L;
        when(userRepository.findById(userId)).thenReturn(null);

        // Act
        var result = userService.getUserById(userId);

        // Assert
        assertFalse(result != null);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserByUsername_UserExists()
    {
        // Arrange
        var username = "testuser";
        var mockUser = new User(username, "test@example.com", "password123", 1L, false, true);

        when(userRepository.findByUsername(username)).thenReturn(mockUser);

        // Act
        var result = userService.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetUserByEmail_UserExists()
    {
        // Arrange
        var email = "test@example.com";
        var mockUser = new User("testuser", email, "password123", 1L, false, true);

        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        // Act
        var result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetAllUsers()
    {
        // Arrange
        var user1 = new User("user1", "u1@example.com", "password123", 1L, false, true);
        var adminUser = new User("admin", "admin@example.com", "adminpass123", 2L, true, true);
        var mockUsers = Arrays.asList(user1, adminUser);

        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        var result = userService.getAllUsers();

        // Assert
        assertEquals(mockUsers, result);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser()
    {
        // Arrange
        var userToUpdate = new User("olduser", "old@example.com", "password123", 1L, false, true);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var updatedUser = userService.updateUser(userToUpdate);

        // Assert
        assertSame(userToUpdate, updatedUser); // Should return the same object instance
        assertEquals(userToUpdate.getId(), updatedUser.getId());
        verify(userRepository, times(1)).save(userToUpdate);
    }

    @Test
    void testDeleteUser()
    {
        // Arrange
        var userId = 1L;

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testAuthenticateUser_Success()
    {
        // Arrange
        var username = "testuser";
        var password = "password123";
        var mockUser = new User(username, "test@example.com", password, 1L, false, false); // Not pre-hashed

        when(userRepository.findByUsername(username)).thenReturn(mockUser);

        // Act
        var result = userService.authenticateUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testAuthenticateUser_InvalidPassword()
    {
        // Arrange
        var username = "testuser";
        var correctPassword = "password123";
        var wrongPassword = "wrongpassword";
        var mockUser = new User(username, "test@example.com", correctPassword, 1L, false, false);

        when(userRepository.findByUsername(username)).thenReturn(mockUser);

        // Act
        var result = userService.authenticateUser(username, wrongPassword);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testAuthenticateUser_UserNotFound()
    {
        // Arrange
        var username = "nonexistent";
        var password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(null);
        when(userRepository.findByEmail(username)).thenReturn(null);

        // Act
        var result = userService.authenticateUser(username, password);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).findByEmail(username);
    }
}
