
package com.example.e_learning.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.e_learning.dto.FeedbackDTO;
import com.example.e_learning.service.FeedbackService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "feedback")
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> submitFeedback(@Valid @RequestBody FeedbackDTO dto, Principal principal) {
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
            response.put( "Feedback submitted successfully", null);
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
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateFeedback(@PathVariable Long id, @Valid @RequestBody FeedbackDTO dto, Principal principal) {
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

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFeedback(@PathVariable Long id, Principal principal) {
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

    @GetMapping("/all")
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbacks() {
        try {
            return ResponseEntity.ok(feedbackService.getAllFeedbacks());
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByCourseId(@PathVariable Long courseId) {
        try {
            return ResponseEntity.ok(feedbackService.getFeedbacksByCourseId(courseId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByCourseId(@PathVariable Long courseId) {
        try {
            Double averageRating = feedbackService.getAverageRatingByCourseId(courseId);
            return ResponseEntity.ok(averageRating);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(0.0);
        }
    }

}
