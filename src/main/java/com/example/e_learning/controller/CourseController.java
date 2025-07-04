package com.example.e_learning.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_learning.dto.CourseDTO;
import com.example.e_learning.dto.EnrollmentDTO;
import com.example.e_learning.entity.Course;
import com.example.e_learning.entity.User;
import com.example.e_learning.service.CourseService;
import com.example.e_learning.service.EnrollmentService;
import com.example.e_learning.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "courses")
@RestController
@RequestMapping("/courses")
public class CourseController {
    @Autowired CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private EnrollmentService enrollmentService;


    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        try {
            return ResponseEntity.ok(courseService.getAllCourses());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            Course course = courseService.createCourse(courseDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course created successfully");
            response.put("data", courseService.convertToDTO(course));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course creation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseDTO courseDTO) {
        try {
            courseService.updateCourse(courseId, courseDTO);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course updated successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course update failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course update failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable Long courseId) {
        try {
            courseService.deleteCourse(courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course deletion failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/enrolled-courses")
    public ResponseEntity<?> getEnrolled(Principal principal) {
        try {
            User currentUser = userService.findByUsername(principal.getName()).orElse(null);
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.status(404).body(error);
            }
            List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUserId(currentUser.getId());
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch enrolled courses");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
