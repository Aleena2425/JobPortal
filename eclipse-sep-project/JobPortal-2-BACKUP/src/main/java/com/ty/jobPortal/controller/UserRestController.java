package com.ty.jobPortal.controller;

import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user operations, primarily exposed for API testing
 * via Swagger UI, not directly used by the Swing application.
 */
@RestController 
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // NOTE: Password must be passed in the body, but masked before returning
            User registeredUser = userService.registerUser(user);
            registeredUser.setPassword("[PROTECTED]"); 
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Catches validation errors like "Username already taken."
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error during registration.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}