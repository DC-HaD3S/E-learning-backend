package com.example.e_learning.controller;

import com.example.e_learning.entity.User;
import com.example.e_learning.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add-bcrypt-prefix")
    public String addBcryptPrefix() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String currentPassword = user.getPassword();
            if (!currentPassword.startsWith("{bcrypt}")) {
                user.setPassword("{bcrypt}" + currentPassword);
                userRepository.save(user);
            }
        }
        return "BCrypt prefixes added successfully";
    }
}