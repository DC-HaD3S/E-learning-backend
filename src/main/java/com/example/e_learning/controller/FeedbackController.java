package com.example.e_learning.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.e_learning.dto.FeedbackDTO;
import com.example.e_learning.dto.HighestRatedCourseDTO;
import com.example.e_learning.dto.InstructorHighestEnrollmentDTO;
import com.example.e_learning.service.CourseService;
import com.example.e_learning.service.FeedbackService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "feedback", description = "Endpoints for managing feedback on courses")
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
        
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);



    @Operation(
        summary = "Submit feedback",
        description = "Allows a user with ROLE_USER to submit feedback for a course. The feedback is associated with the authenticated user's username.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feedback submitted successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., missing course ID)", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "User not authenticated or unauthorized", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Server error", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitFeedback(
        @Valid @RequestBody @Parameter(description = "Feedback details including course ID and feedback content", required = true) 
        FeedbackDTO dto, 
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            if (principal == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            dto.setUsername(principal.getName());
            if (dto.getCourseId() == null) {
                response.put("error", "Course ID cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            feedbackService.submitFeedback(dto);
            response.put("message", "Feedback submitted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Feedback submission failed: " + e.getMessage());
            e.printStackTrace(); // Log stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Update feedback",
        description = "Allows a user with ROLE_USER to update their own feedback identified by feedback ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feedback updated successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or feedback not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "User not authorized to update feedback", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateFeedback(
        @Parameter(description = "ID of the feedback to update", required = true) 
        @PathVariable Long id, 
        @Valid @RequestBody @Parameter(description = "Updated feedback details", required = true) 
        FeedbackDTO dto, 
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            dto.setUsername(principal.getName());
            feedbackService.updateFeedback(id, dto);
            response.put("message", "Feedback updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Feedback update failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Delete feedback",
        description = "Allows a user with ROLE_USER to delete their own feedback or an admin with ROLE_ADMIN to delete any feedback, identified by feedback ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feedback deleted successfully", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or feedback not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete feedback", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
        }
    )
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFeedback(
        @Parameter(description = "ID of the feedback to delete", required = true) 
        @PathVariable Long id, 
        @Parameter(description = "Authenticated user's principal", hidden = true) 
        Principal principal) {
        Map<String, String> response = new HashMap<>();
        try {
            feedbackService.deleteFeedback(id, principal.getName());
            response.put("message", "Feedback deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Feedback deletion failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Get all feedback",
        description = "Retrieves all feedback entries. Accessible to all authenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of all feedback", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
        }
    )
    @GetMapping("/all")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbacks() {
        try {
            return ResponseEntity.ok(feedbackService.getAllFeedbacks());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(
        summary = "Get feedback by course ID",
        description = "Retrieves all feedback for a specific course identified by course ID. Accessible to all authenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of feedback for the specified course", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeedbackDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request or course not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
        }
    )
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByCourseId(
        @Parameter(description = "ID of the course to retrieve feedback for", required = true) 
        @PathVariable Long courseId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCourseId(courseId));
    }
    

    @Operation(
            summary = "Get total feedback count for an instructor's courses",
            description = "Retrieves the total number of feedback entries for all courses taught by a specific instructor, identified by instructor application ID. Accessible to all users, including unauthenticated users.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Total feedback count for the instructor's courses", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
                @ApiResponse(responseCode = "400", description = "Invalid instructor ID", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
                @ApiResponse(responseCode = "500", description = "Server error", 
                             content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
            }
        )
        @GetMapping("/instructor/{instructorId}/feedback-count")
        public ResponseEntity<?> getFeedbackCountByInstructorId(
            @Parameter(description = "ID of the instructor application to retrieve feedback count for", required = true) 
            @PathVariable Long instructorId) {
            try {
                Long feedbackCount = feedbackService.getFeedbackCountByInstructorId(instructorId);
                return ResponseEntity.ok(feedbackCount);
            } catch (Exception e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to retrieve feedback count: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    

    @Operation(
        summary = "Get average rating by course ID",
        description = "Calculates and returns the average rating for a specific course identified by course ID. Accessible to all authenticated users.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Average rating for the specified course", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "400", description = "Bad request or course not found", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Double.class)))
        }
    )
    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByCourseId(
        @Parameter(description = "ID of the course to calculate average rating for", required = true) 
        @PathVariable Long courseId) {
        Double averageRating = feedbackService.getAverageRatingByCourseId(courseId);
        return ResponseEntity.ok(averageRating);
    }
	    
	    @Operation(
	            summary = "Get course with highest average rating",
	            description = "Public endpoint to retrieve the course ID, title, and average rating of the course with the highest average rating based on feedback. Accessible to all users, including unauthenticated users.")
	    @GetMapping("/highest-rated-courses")
	    public ResponseEntity<?> getHighestRatedCourses() {
	        try {
	            List<HighestRatedCourseDTO> courses = feedbackService.getHighestRatedCourses();
	            return ResponseEntity.ok(courses);
	        } catch (Exception e) {
	            logger.error("Failed to retrieve highest rated courses: {}", e.getMessage());
	            Map<String, String> errorResponse = Map.of("error", "Failed to retrieve highest rated courses: " + e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	        }
	    }
	    


	}

