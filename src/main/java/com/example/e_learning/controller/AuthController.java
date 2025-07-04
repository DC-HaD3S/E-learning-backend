package com.example.e_learning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.e_learning.dto.JwtRequest;
import com.example.e_learning.dto.JwtResponse;
import com.example.e_learning.dto.SignupRequest;
import com.example.e_learning.service.JwtService;
import com.example.e_learning.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@Tag(name = "auth")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        final UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        final String jwt = jwtService.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            userService.registerUser(signupRequest);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already registered");
        }
        return ResponseEntity.ok("Username is available");
    }

    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }
        return ResponseEntity.ok("Email is available");
    }
}