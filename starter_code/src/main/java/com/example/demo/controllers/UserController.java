package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/id/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.of(userRepository.findById(id));
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> findByUserName(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("No user found");
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(user);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
        try {
            User user = new User();
            user.setUsername(createUserRequest.getUsername());
            Cart cart = new Cart();
            cartRepository.save(cart);
            user.setCart(cart);
            if (createUserRequest.getPassword().length() < 7 ||
                    !createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())) {
                logger.error("Provided password does not meet the required criteria.");
                return ResponseEntity.badRequest().build();
            }
            user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
            userRepository.save(user);
            logger.info("User created successfully");
            return ResponseEntity.ok(user);
        }catch (Exception ex){
            logger.error("Error while creating User");
            return ResponseEntity.notFound().build();
        }
    }

}
