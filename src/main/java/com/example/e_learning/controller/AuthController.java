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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "auth", description = "Authentication and user registration endpoints")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;

    @Operation(
        summary = "User login",
        description = "Authenticates a user with username and password, returning a JWT token upon successful authentication.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Incorrect username or password", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody 
        @Parameter(description = "Login credentials containing username and password", required = true) 
        JwtRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect username or password"));
        }

        final UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        final String jwt = jwtService.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @Operation(
        summary = "User registration",
        description = "Registers a new user with provided details (name, email, username, password). Role is set to USER by default.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: Username or email already registered", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody 
        @Parameter(description = "User registration details", required = true) 
        SignupRequest signupRequest) {
        try {
            userService.registerUser(signupRequest);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Check username availability",
        description = "Checks if a username is available for registration.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Username is available", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Username already registered", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, String>> checkUsername(
        @Parameter(description = "Username to check for availability", required = true) 
        @RequestParam String username) {
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already registered"));
        }
        return ResponseEntity.ok(Map.of("message", "Username is available"));
    }

    @Operation(
        summary = "Check email availability",
        description = "Checks if an email is available for registration.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Email is available", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, String>> checkEmail(
        @Parameter(description = "Email to check for availability", required = true) 
        @RequestParam String email) {
        if (userService.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered"));
        }
        return ResponseEntity.ok(Map.of("message", "Email is available"));
    }
}