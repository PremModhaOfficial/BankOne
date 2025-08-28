package com.bank.db.inmemory;

import com.bank.business.entities.User;
import com.bank.business.repositories.UserRepository;

import java.util.ArrayList; // Add import
import java.util.List; // Add import
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository
{
    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1); // Simple ID generator

    private InMemoryUserRepository()
    {
    }

    public static InMemoryUserRepository getInstance()
    {
        return InMemoryUserRepositorySingleton.INSTANCE;
    }

    @Override
    public User save(User user)
    {
        if (user.getId() == null)
        {
            user.setId((long) user.hashCode());
            idGenerator.getAndIncrement();
        }
        if (userStore.containsValue(user))
        {
            userStore.put(user.getId(), user);
            return userStore.get(user.getId());
        }

        try
        {

        } catch (Exception e)
        {
        }

        userStore.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(Long id)
    {
        return userStore.get(id);
    }

    @Override
    public User findByUsername(String username)
    {
        return userStore.values().stream().filter(user -> username.equals(user.getUsername())).findFirst().orElse(null);
    }

    @Override
    public User findByEmail(String email)
    {
        return userStore.values().stream().filter(user -> email.equals(user.getEmail())).findFirst().orElse(null);
    }

    @Override
    public void deleteById(Long id)
    {
        userStore.remove(id);
    }

    @Override
    public List<User> findAll()
    {
        // Implement the new method
        // Return a copy to prevent external modification of the internal store
        return new ArrayList<>(userStore.values());
    }

    private static class InMemoryUserRepositorySingleton
    {
        private static final InMemoryUserRepository INSTANCE = new InMemoryUserRepository();
    }
}
