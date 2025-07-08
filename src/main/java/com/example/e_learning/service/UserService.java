package com.example.e_learning.service;

import com.example.e_learning.dto.SignupRequest;
import com.example.e_learning.dto.UserDTO;
import com.example.e_learning.entity.User;
import com.example.e_learning.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
//c
    @Transactional
    public void registerUser(SignupRequest signupRequest) {
        // Pre-checks
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already registered");
        }
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        String role = signupRequest.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        } else if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Invalid role: must be 'USER' or 'ADMIN'");
        } else if (role.equals("ADMIN")) {
            throw new IllegalArgumentException("Admin registration is not allowed via this endpoint");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(role);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            if (e.getCause() instanceof DataIntegrityViolationException) {
                if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
                    throw new IllegalArgumentException("Username already registered");
                }
                if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("Email already registered");
                }
            }
            throw new IllegalArgumentException("Registration failed: " + e.getMessage());
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setUsername(user.getUsername());
            dto.setPassword(user.getPassword());
            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalStateException("User role cannot be empty for username: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
        );
    }

    @Transactional
    public void updateUserDetails(String email, UserDTO updatedUser) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(updatedUser.getName());
        user.setUsername(updatedUser.getUsername());

        if (!updatedUser.getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("Email updates are not allowed.");
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }
}
