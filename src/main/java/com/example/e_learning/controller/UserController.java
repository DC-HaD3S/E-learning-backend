package com.example.e_learning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.e_learning.dto.EnrollmentDTO;
import com.example.e_learning.dto.UserDTO;
import com.example.e_learning.entity.User;
import com.example.e_learning.service.EnrollmentService;
import com.example.e_learning.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "users", description = "Endpoints for managing user enrollment and user details")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private EnrollmentService enrollmentService;

    @Operation(
        summary = "Enroll user in a course",
        description = "Allows a user with ROLE_USER to enroll in a course by providing the course ID. The enrollment is associated with the authenticated user's username.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User enrolled successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., missing course ID)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "User not authenticated or unauthorized", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PostMapping("/apply-course")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> enroll(
        @Valid @RequestBody @Parameter(description = "Enrollment details containing the course ID", required = true) 
        EnrollmentDTO enrollmentDTO, 
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            if (enrollmentDTO.getCourseId() == null) {
                response.put("message", "Course ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            enrollmentService.enrollUserToCourse(principal.getName(), enrollmentDTO.getCourseId());
            response.put("message", "User enrolled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Enrollment failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Get all users",
        description = "Allows an admin with ROLE_ADMIN to retrieve a list of all users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of all users", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
        summary = "Get all enrolled users",
        description = "Allows an admin with ROLE_ADMIN to retrieve a list of all course enrollments.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of all enrollments", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrolledUsers() {
        try {
            return ResponseEntity.ok(enrollmentService.getAllEnrollments());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @Operation(
        summary = "Update user details",
        description = "Allows an admin with ROLE_ADMIN to update any user's details or a user with ROLE_USER to update their own details, identified by email.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User details updated successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: User cannot update another user's details", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateUserDetails(
            @Parameter(description = "Email of the user to update", required = true) 
            @RequestParam String userEmail,
            @Valid @RequestBody @Parameter(description = "Updated user details", required = true) 
            UserDTO updatedUser,
            @Parameter(description = "Authenticated user's principal", hidden = true) 
            Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            String requesterUsername = principal.getName();
            User requester = userService.findByUsername(requesterUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Requester not found"));
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));
            boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole());
            if (!isAdmin && !requester.getEmail().equalsIgnoreCase(userEmail)) {
                response.put("message", "Access denied: you can only update your own details");
                return ResponseEntity.status(403).body(response);
            }
            userService.updateUserDetails(userEmail, updatedUser);
            response.put("message", "User details updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Update failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(
        summary = "Delete user by email",
        description = "Allows an admin with ROLE_ADMIN to delete a user identified by email.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserByEmail(
            @Parameter(description = "Email of the user to delete", required = true) 
            @RequestParam String userEmail) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.deleteUserByEmail(userEmail);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Deletion failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}