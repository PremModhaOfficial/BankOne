package com.bank.business.services;

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        var savedUser = new User(username, email, false); // Explicitly non-admin
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        var result = userService.createUser(username, email);

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
        var isAdmin = true;
        var savedUser = new User(username, email, isAdmin);
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        var result = userService.createUser(username, email, isAdmin);

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
        var mockUser = new User("testuser", "test@example.com");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn((mockUser));

        // Act
        var result = userService.getUserById(userId);

        // Assert
        assertTrue(result != null);
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
        var mockUser = new User(username, "test@example.com");
        mockUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act
        var result = userService.getUserByUsername(username);

        // Assert
        assertTrue(result != null);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetUserByEmail_UserExists()
    {
        // Arrange
        var email = "test@example.com";
        var mockUser = new User("testuser", email);
        mockUser.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(null);

        // Act
        var result = userService.getUserByEmail(email);

        // Assert
        assertTrue(result != null);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetAllUsers()
    {
        // Arrange
        var user1 = new User("user1", "u1@example.com");
        user1.setId(1L);
        var adminUser = new User("admin", "admin@example.com", true);
        adminUser.setId(2L);
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
        var userToUpdate = new User("olduser", "old@example.com");
        userToUpdate.setId(1L);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var updatedUser = userService.updateUser(userToUpdate);

        // Assert
        assertSame(userToUpdate, updatedUser); // Should return the same object instance
        assertEquals(userToUpdate.getId(), updatedUser.getId());
        // Verify updatedAt was updated (it should be more recent than createdAt)
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
}
