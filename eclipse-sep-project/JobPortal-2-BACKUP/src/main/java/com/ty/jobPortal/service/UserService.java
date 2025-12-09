package com.ty.jobPortal.service;

import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Simplified password hashing
    private String hashPassword(String password) {
        return "HASHED_" + password;
    }

    // Simplified password check
    private boolean checkPassword(String rawPassword, String storedHashedPassword) {
        return hashPassword(rawPassword).equals(storedHashedPassword);
    }

    public User registerUser(User user) {
        // FIX: Service Validation Check
        if (user.getUsername() == null || user.getUsername().isBlank() || 
            user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Username and Password are required for registration.");
        }
        
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken.");
        }
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new SecurityException("User not found.");
        }

        User user = userOpt.get();
        if (checkPassword(password, user.getPassword())) {
            return user;
        } else {
            throw new SecurityException("Invalid password.");
        }
    }
}