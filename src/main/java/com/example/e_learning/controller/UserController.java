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
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "users")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private EnrollmentService enrollmentService;

    @PostMapping("/apply-course")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> enroll(@Valid @RequestBody EnrollmentDTO enrollmentDTO, Principal principal) {
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrolledUsers() {
        try {
            return ResponseEntity.ok(enrollmentService.getAllEnrollments());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateUserDetails(
            @RequestParam String userEmail,
            @Valid @RequestBody UserDTO updatedUser,
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

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUserByEmail(@RequestParam String userEmail) {
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