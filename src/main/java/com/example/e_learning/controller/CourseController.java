
package com.example.e_learning.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_learning.dto.CourseDTO;
import com.example.e_learning.dto.EnrollmentDTO;
import com.example.e_learning.dto.HighestEnrollmentDTO;
import com.example.e_learning.entity.Course;
import com.example.e_learning.entity.User;
import com.example.e_learning.service.CourseService;
import com.example.e_learning.service.EnrollmentService;
import com.example.e_learning.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@Tag(name = "courses", description = "Endpoints for managing courses, including creation, update, deletion, and enrollment")
@RestController
@RequestMapping("/courses")
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired private CourseService courseService;
    @Autowired private UserService userService;
    @Autowired private EnrollmentService enrollmentService;

    @Operation(
        summary = "Get all courses",
        description = "Public endpoint to view all courses, accessible to unauthenticated users, users, instructors, and admins. Returns course details including title, body, image URL, price, and instructor ID (if assigned).",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of all courses with their details", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class, type = "array"))),
            @ApiResponse(responseCode = "400", description = "Bad request, returns empty list", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class, type = "array")))
        }
    )
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        try {
            return ResponseEntity.ok(courseService.getAllCourses());
        } catch (Exception e) {
            logger.error("Error retrieving all courses: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @Operation(
        summary = "Get instructor's own courses",
        description = "Allows an instructor to view only their own courses, based on their instructor application ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of instructor's courses", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseDTO.class, type = "array"))),
            @ApiResponse(responseCode = "400", description = "Bad request or user not found, returns error message", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Instructor access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "No courses or instructor application found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses(
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        try {
            List<CourseDTO> courseDTOs = courseService.getCoursesByInstructor();
            return ResponseEntity.ok(courseDTOs);
        } catch (IllegalStateException e) {
            logger.error("Error fetching courses: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching courses: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Create a new course",
        description = "Allows an admin or instructor to create a course. The course is associated with the authenticated instructor's application ID if created by an instructor. Admins can create courses without an instructor, to be assigned later.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Course created successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"message\": \"Course created successfully\", \"data\": {\"id\": 1, \"title\": \"string\", \"body\": \"string\", \"imageUrl\": \"string\", \"price\": 0.0, \"instructorId\": 1}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin or instructor access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Map<String, Object>> createCourse(
        @Valid @RequestBody @Parameter(description = "Course details to create, including instructorId for instructors", required = true) 
        CourseDTO courseDTO,
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        try {
            Course course = courseService.createCourse(courseDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course created successfully");
            response.put("data", courseService.convertToDTO(course));
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course creation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(
        summary = "Update a course",
        description = "Allows an admin to update any course or an instructor to update their own course.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, example = "{\"message\": \"Course updated successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin or course owner access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Course or user not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> updateCourse(
        @Parameter(description = "ID of the course to update", required = true) 
        @PathVariable Long courseId,
        @Valid @RequestBody @Parameter(description = "Updated course details", required = true) 
        CourseDTO courseDTO,
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        try {
            courseService.updateCourse(courseId, courseDTO);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course updated successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course update failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(
        summary = "Delete a course",
        description = "Allows an admin to delete any course or an instructor to delete their own course.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Course deleted successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, example = "{\"message\": \"Course deleted successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin or course owner access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Course or user not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Map<String, String>> deleteCourse(
        @Parameter(description = "ID of the course to delete", required = true) 
        @PathVariable Long courseId,
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        try {
            courseService.deleteCourse(courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Course deletion failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(
        summary = "Set or update instructor for a course",
        description = "Allows an admin to set or update the instructor for a specific course using the course ID and instructor application ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Instructor set successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, example = "{\"message\": \"Instructor set successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., course or instructor not found, missing instructor ID)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: Admin access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}/set-instructor")
    public ResponseEntity<Map<String, String>> setCourseInstructor(
        @Parameter(description = "ID of the course to set or update instructor for", required = true) 
        @PathVariable Long courseId,
        @Parameter(description = "ID of the instructor application", required = true) 
        @RequestParam Long instructorId) {
        try {
            courseService.setCourseInstructor(courseId, instructorId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Instructor set successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to set instructor: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @Operation(
        summary = "Get enrollment count for a specific course",
        description = "Public endpoint to retrieve the total number of enrolled users for a specific course, identified by course ID. Accessible to all users, including unauthenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Enrollment count for the course", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, example = "{\"count\": 10}"))),
            @ApiResponse(responseCode = "400", description = "Invalid course ID", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @GetMapping("/{courseId}/enrollment-count")
    public ResponseEntity<?> getEnrollmentCountByCourseId(
        @Parameter(description = "ID of the course to retrieve enrollment count for", required = true) 
        @PathVariable Long courseId) {
        try {
            Long enrollmentCount = courseService.getEnrollmentCountByCourseId(courseId);
            Map<String, Long> response = Map.of("count", enrollmentCount);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid course ID {}: {}", courseId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to retrieve enrollment count for course ID {}: {}", courseId, e.getMessage());
            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve enrollment count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(
        summary = "Get enrolled courses",
        description = "Allows a user or admin to view their enrolled courses.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of enrolled courses", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class, type = "array"))),
            @ApiResponse(responseCode = "400", description = "Bad request, returns empty list", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Unauthorized: User or admin access required", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class, type = "array"))),
            @ApiResponse(responseCode = "404", description = "User not found, returns empty list", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDTO.class, type = "array")))
        }
    )
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/enrolled-courses")
    public ResponseEntity<List<EnrollmentDTO>> getEnrolled(
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        try {
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUserId(currentUser.getId());
            return ResponseEntity.ok(enrollments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
    @Operation(
            summary = "Get course with highest enrolled users count",
            description = "Public endpoint to retrieve the course ID and number of users enrolled in the course with the highest enrollment count. Accessible to all users, including unauthenticated users.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Course ID and count of users enrolled in the course with the highest enrollments", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = HighestEnrollmentDTO.class, example = "{\"courseId\": 7, \"count\": 10}"))),
                @ApiResponse(responseCode = "404", description = "No enrollments found", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class, example = "{\"courseId\": null, \"count\": 0}"))),
                @ApiResponse(responseCode = "500", description = "Server error", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
            }
        )
    @GetMapping("/highest-enrolled-users-count")
    public ResponseEntity<?> getHighestEnrolledUsersCount() {
        try {
            HighestEnrollmentDTO enrollment = courseService.getHighestEnrolledUsersCount();
            return ResponseEntity.ok(enrollment);
        } catch (Exception e) {
            logger.error("Failed to retrieve highest enrolled users count: {}", e.getMessage());
            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve highest enrolled users count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
