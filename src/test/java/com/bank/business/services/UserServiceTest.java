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
        String username = "testuser";
        String email = "test@example.com";
        User savedUser = new User(username, email, false); // Explicitly non-admin
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        User result = userService.createUser(username, email);

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
        String username = "adminuser";
        String email = "admin@example.com";
        boolean isAdmin = true;
        User savedUser = new User(username, email, isAdmin);
        savedUser.setId(1L); // Simulate ID assignment by repo

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID assignment
            return user;
        });

        // Act
        User result = userService.createUser(username, email, isAdmin);

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
        Long userId = 1L;
        User mockUser = new User("testuser", "test@example.com");
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
    void testGetUserById_UserNotFound()
    {
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
    void testGetUserByUsername_UserExists()
    {
        // Arrange
        String username = "testuser";
        User mockUser = new User(username, "test@example.com");
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
    void testGetUserByEmail_UserExists()
    {
        // Arrange
        String email = "test@example.com";
        User mockUser = new User("testuser", email);
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
    void testGetAllUsers()
    {
        // Arrange
        User user1 = new User("user1", "u1@example.com");
        user1.setId(1L);
        User adminUser = new User("admin", "admin@example.com", true);
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
    void testUpdateUser()
    {
        // Arrange
        User userToUpdate = new User("olduser", "old@example.com");
        userToUpdate.setId(1L);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User updatedUser = userService.updateUser(userToUpdate);

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
        Long userId = 1L;

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
}
